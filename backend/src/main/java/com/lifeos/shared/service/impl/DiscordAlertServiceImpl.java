package com.lifeos.shared.service.impl;

import com.lifeos.shared.service.DiscordAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DiscordAlertServiceImpl implements DiscordAlertService {

    @Value("${app.discord.webhook-url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    // Rate limiting: track last send time per alert type
    private final Map<String, Instant> lastSentMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_SECONDS = 300; // 5 minutes

    public DiscordAlertServiceImpl() {
        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    @Override
    public void sendSyncAlert(String deviceId, int sessions, int logs) {
        String message = String.format("✅ **Sync completed** from `%s`\n" +
            "📦 Sessions: **%d** | 📝 Logs: **%d**", deviceId, sessions, logs);
        sendMessage("sync_completed", message);
    }

    @Override
    public void sendOfflineWarning(String deviceId) {
        String message = String.format("⚠️ **No sync received in 24 hours** from `%s`\n" +
            "Device may be offline or tracker is not running.", deviceId);
        sendMessage("offline_warning_" + deviceId, message);
    }

    @Override
    public void sendErrorLogAlert(String deviceId, String tag, String errorMessage) {
        String message = String.format("🔴 **ERROR log detected** from `%s`\n" +
            "**Tag:** `%s`\n**Message:** ```%s```", deviceId, tag, errorMessage);
        sendMessage("error_log", message);
    }

    @Override
    public void sendDailySummary(Map<String, String> activitySummary, String dateLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **Daily Activity Summary** — %s\n\n", dateLabel));

        if (activitySummary.isEmpty()) {
            sb.append("No activity recorded today.");
        } else {
            Map<String, String> icons = Map.of(
                "GAME", "🎮",
                "MUSIC", "🎵",
                "VIDEO", "🎬",
                "CODING", "💻",
                "BROWSER", "🌐"
            );

            for (Map.Entry<String, String> entry : activitySummary.entrySet()) {
                String icon = icons.getOrDefault(entry.getKey().toUpperCase(), "📌");
                sb.append(String.format("%s **%s:** %s\n", icon, entry.getKey(), entry.getValue()));
            }
        }

        sendMessage("daily_summary", sb.toString());
    }

    private void sendMessage(String alertType, String content) {
        if (!isConfigured()) {
            log.debug("Discord webhook not configured, skipping alert: {}", alertType);
            return;
        }

        // Rate limiting check
        Instant lastSent = lastSentMap.get(alertType);
        if (lastSent != null && Instant.now().minusSeconds(RATE_LIMIT_SECONDS).isBefore(lastSent)) {
            log.debug("Rate limiting Discord alert '{}', skipping", alertType);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("content", content);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);
            lastSentMap.put(alertType, Instant.now());
            log.info("Discord alert sent: {}", alertType);
        } catch (Exception e) {
            log.error("Failed to send Discord alert '{}': {}", alertType, e.getMessage());
        }
    }
}
