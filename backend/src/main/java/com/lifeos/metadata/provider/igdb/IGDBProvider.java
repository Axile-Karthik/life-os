package com.lifeos.metadata.provider.igdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.metadata.dto.SearchResultDto;
import com.lifeos.metadata.entity.ExternalProviderTokenEntity;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.provider.MetadataProvider;
import com.lifeos.metadata.token.ProviderTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IGDBProvider implements MetadataProvider {

    private static final String PROVIDER_NAME = "IGDB";
    private static final String TWITCH_AUTH_URL = "https://id.twitch.tv/oauth2/token";
    private static final String IGDB_API_URL = "https://api.igdb.com/v4/games";

    private final String clientId;
    private final String clientSecret;
    private final ProviderTokenService tokenService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public IGDBProvider(
            @Value("${igdb.client-id:}") String clientId,
            @Value("${igdb.client-secret:}") String clientSecret,
            ProviderTokenService tokenService,
            ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    @Override
    public boolean supports(ContentType contentType) {
        return contentType == ContentType.GAME || contentType == ContentType.ALL;
    }

    @Override
    public List<SearchResultDto> search(String query) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.warn("IGDB credentials not configured. Skipping IGDB search.");
            return List.of();
        }

        String accessToken = tokenService.getAccessToken(PROVIDER_NAME, this::fetchNewTwitchToken);

        // Filter out non-games: 0=main_game, 4=expansion, 8=remake, 9=remaster, 10=expanded_game, 11=port
        String apicalypseQuery = String.format(
            "search \"%s\";\n" +
            "fields id,name,cover.url,first_release_date,category;\n" +
            "where category = (0,4,8,9,10,11);\n" +
            "limit 10;",
            query.replace("\"", "\\\"")
        );

        log.info("Sending search request to IGDB for query: {}", query);

        try {
            String response = restClient.post()
                    .uri(IGDB_API_URL)
                    .header("Client-ID", clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(apicalypseQuery)
                    .retrieve()
                    .body(String.class);

            return parseResponse(response);

        } catch (Exception e) {
            log.error("Failed to search IGDB for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    private ExternalProviderTokenEntity fetchNewTwitchToken() {
        log.info("Requesting new Twitch OAuth token for IGDB...");

        String uri = String.format("%s?client_id=%s&client_secret=%s&grant_type=client_credentials",
                TWITCH_AUTH_URL, clientId, clientSecret);

        try {
            JsonNode responseNode = restClient.post()
                    .uri(uri)
                    .retrieve()
                    .body(JsonNode.class);

            if (responseNode != null && responseNode.has("access_token")) {
                String accessToken = responseNode.get("access_token").asText();
                long expiresInSeconds = responseNode.has("expires_in") ? responseNode.get("expires_in").asLong() : 3600;

                return ExternalProviderTokenEntity.builder()
                        .accessToken(accessToken)
                        .expiresAt(Instant.now().plusSeconds(expiresInSeconds))
                        .build();
            } else {
                throw new RuntimeException("Invalid token response from Twitch: " + responseNode);
            }
        } catch (Exception e) {
            log.error("Error fetching Twitch token: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch Twitch token", e);
        }
    }

    private List<SearchResultDto> parseResponse(String jsonResponse) {
        List<SearchResultDto> results = new ArrayList<>();
        try {
            JsonNode arrayNode = objectMapper.readTree(jsonResponse);
            if (arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    SearchResultDto dto = new SearchResultDto();
                    dto.setContentType(ContentType.GAME);
                    dto.setExternalSource(PROVIDER_NAME);

                    if (node.has("id")) {
                        dto.setExternalId(String.valueOf(node.get("id").asLong()));
                        // Temporarily use external ID as local ID for ephemeral discovery results
                        dto.setId(PROVIDER_NAME + "_" + dto.getExternalId());
                    }

                    if (node.has("name")) {
                        dto.setTitle(node.get("name").asText());
                    }

                    if (node.has("first_release_date")) {
                        long unixTimestamp = node.get("first_release_date").asLong();
                        LocalDate releaseDate = Instant.ofEpochSecond(unixTimestamp).atZone(ZoneOffset.UTC).toLocalDate();
                        dto.setReleaseDate(releaseDate);
                    }

                    if (node.has("cover") && node.get("cover").has("url")) {
                        String rawUrl = node.get("cover").get("url").asText();
                        // Normalize URL: //images.igdb.com/ -> https://images.igdb.com/
                        if (rawUrl != null && rawUrl.startsWith("//")) {
                            rawUrl = "https:" + rawUrl;
                        }
                        dto.setImageUrl(rawUrl);
                    }

                    results.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse IGDB response: {}", e.getMessage());
        }
        return results;
    }
}
