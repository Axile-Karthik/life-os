package com.lifeos.telemetry.session.scheduler;

import com.lifeos.common.service.DiscordAlertService;
import com.lifeos.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionScheduler {

    private final DiscordAlertService discordAlertService;
    private final SessionService sessionService;

    /**
     * Every midnight (server time): send daily summary.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightDailySummary() {
        log.info("Running midnight scheduled task: send daily summary");
        sendDailySummary();
    }

    /**
     * Every hour: check if any device has gone offline (no sync in 24h).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyOfflineCheck() {
        log.debug("Running hourly offline check");
        checkDeviceOffline("karthik-phone");
    }

    private void sendDailySummary() {
        try {
            LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
            Instant startOfDay = yesterday.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endOfDay = yesterday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            Map<String, Long> durationByType = sessionService.getDurationByType(startOfDay, endOfDay);

            Map<String, String> formattedSummary = new LinkedHashMap<>();
            for (Map.Entry<String, Long> entry : durationByType.entrySet()) {
                formattedSummary.put(entry.getKey(), SessionService.formatDuration(entry.getValue()));
            }

            String dateLabel = yesterday.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            discordAlertService.sendDailySummary(formattedSummary, dateLabel);

        } catch (Exception e) {
            log.error("Failed to send daily summary: {}", e.getMessage(), e);
        }
    }

    private void checkDeviceOffline(String deviceId) {
        try {
            Instant lastSync = sessionService.getLastSyncTime(deviceId);
            if (lastSync != null && lastSync.isBefore(Instant.now().minus(24, ChronoUnit.HOURS))) {
                log.warn("Device {} has not synced in 24 hours (last sync: {})", deviceId, lastSync);
                discordAlertService.sendOfflineWarning(deviceId);
            }
        } catch (Exception e) {
            log.error("Failed to check device offline status: {}", e.getMessage());
        }
    }
}
