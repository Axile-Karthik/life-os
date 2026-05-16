package com.lifeos.metadata.token;

import com.lifeos.metadata.entity.ExternalProviderTokenEntity;
import com.lifeos.metadata.repository.ExternalProviderTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderTokenService {

    private final ExternalProviderTokenRepository tokenRepository;

    // In-memory cache
    private final Map<String, ExternalProviderTokenEntity> tokenCache = new ConcurrentHashMap<>();

    /**
     * Retrieves an access token for the given provider.
     * Checks in-memory cache first, then database.
     * If missing or expired, it uses the provided fetcher to get a new token, caches it, and saves it to the DB.
     *
     * @param provider The provider name (e.g., "IGDB")
     * @param fetchNewToken Logic to fetch a new token from the external provider if needed
     * @return The active access token
     */
    public String getAccessToken(String provider, Supplier<ExternalProviderTokenEntity> fetchNewToken) {
        ExternalProviderTokenEntity cachedToken = tokenCache.get(provider);

        if (isValid(cachedToken)) {
            return cachedToken.getAccessToken();
        }

        // Cache miss or expired. Try DB.
        Optional<ExternalProviderTokenEntity> dbTokenOpt = tokenRepository.findByProvider(provider);
        if (dbTokenOpt.isPresent() && isValid(dbTokenOpt.get())) {
            ExternalProviderTokenEntity dbToken = dbTokenOpt.get();
            tokenCache.put(provider, dbToken);
            return dbToken.getAccessToken();
        }

        // DB miss or expired. Fetch new token.
        log.info("Fetching new access token for provider: {}", provider);
        ExternalProviderTokenEntity newToken = fetchNewToken.get();
        newToken.setProvider(provider);

        // Save to DB and Cache
        // If it already existed in DB but was expired, we update the existing record
        if (dbTokenOpt.isPresent()) {
            ExternalProviderTokenEntity existing = dbTokenOpt.get();
            existing.setAccessToken(newToken.getAccessToken());
            existing.setRefreshToken(newToken.getRefreshToken());
            existing.setExpiresAt(newToken.getExpiresAt());
            existing.setMetadata(newToken.getMetadata());
            newToken = tokenRepository.save(existing);
        } else {
            newToken = tokenRepository.save(newToken);
        }

        tokenCache.put(provider, newToken);
        return newToken.getAccessToken();
    }

    private boolean isValid(ExternalProviderTokenEntity token) {
        if (token == null || token.getAccessToken() == null) {
            return false;
        }
        // If it expires within the next 5 minutes, consider it expired to be safe
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now().plusSeconds(300))) {
            return false;
        }
        return true;
    }
}
