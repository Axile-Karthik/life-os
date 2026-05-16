package com.lifeos.telemetry.sync.service;
import com.lifeos.common.service.DiscordAlertService;

import com.lifeos.telemetry.session.dto.*;
import com.lifeos.observability.metrics.dto.*;
import com.lifeos.telemetry.sync.dto.*;
import com.lifeos.telemetry.session.entity.*;
import com.lifeos.observability.metrics.entity.*;
import com.lifeos.observability.log.dto.TrackerLogDto;
import com.lifeos.observability.log.entity.TrackerLog;
import com.lifeos.observability.log.repository.TrackerLogRepository;
import com.lifeos.telemetry.session.repository.*;
import com.lifeos.observability.metrics.repository.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Core sync service. Handles the POST /api/sync payload:
 * - Batch-inserts activity sessions (skipping duplicates via unique constraint)
 * - Saves tracker logs
 * - Saves app metrics snapshot
 * - Fires Discord alerts for sync completion and ERROR logs
 * - Records Micrometer metrics for observability
 */
@Slf4j
@Service
public class SyncService {

    private final ActivitySessionRepository sessionRepository;
    private final TrackerLogRepository logRepository;
    private final AppMetricsRepository metricsRepository;
    private final DiscordAlertService discordAlertService;

    // Micrometer counters for observability
    private final Counter syncCounter;
    private final Counter sessionsSyncedCounter;
    private final Counter logsSyncedCounter;
    private final Counter duplicateSessionsCounter;
    private final Counter syncErrorCounter;
    private final Timer syncTimer;

    // Latest metrics for Gauges
    private Double latestHeapUsedMb = 0.0;
    private Double latestHeapMaxMb = 0.0;
    private Long latestScanDurationMs = 0L;
    private Integer latestBatteryLevel = 0;
    private Integer latestPendingSessions = 0;
    private Integer latestPendingLogs = 0;
    private Boolean latestIsCharging = false;
    private Boolean latestIsWifi = false;

    public SyncService(ActivitySessionRepository sessionRepository,
                       TrackerLogRepository logRepository,
                       AppMetricsRepository metricsRepository,
                       DiscordAlertService discordAlertService,
                       MeterRegistry meterRegistry) {
        this.sessionRepository = sessionRepository;
        this.logRepository = logRepository;
        this.metricsRepository = metricsRepository;
        this.discordAlertService = discordAlertService;

        this.syncCounter = Counter.builder("lifeos.sync.total")
            .description("Total sync requests received")
            .register(meterRegistry);
        this.sessionsSyncedCounter = Counter.builder("lifeos.sync.sessions")
            .description("Total sessions synced")
            .register(meterRegistry);
        this.logsSyncedCounter = Counter.builder("lifeos.sync.logs")
            .description("Total logs synced")
            .register(meterRegistry);
        this.duplicateSessionsCounter = Counter.builder("lifeos.sync.duplicates")
            .description("Total duplicate sessions skipped")
            .register(meterRegistry);
        this.syncErrorCounter = Counter.builder("lifeos.sync.errors")
            .description("Total sync errors")
            .register(meterRegistry);
        this.syncTimer = Timer.builder("lifeos.sync.duration")
            .description("Sync request processing time")
            .register(meterRegistry);

        // Register Gauges for Telemetry
        Gauge.builder("lifeos_tracker_heap_used_mb", () -> latestHeapUsedMb)
            .description("Latest heap used MB from tracker")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_heap_max_mb", () -> latestHeapMaxMb)
            .description("Latest heap max MB from tracker")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_scan_duration_ms", () -> latestScanDurationMs)
            .description("Latest scan duration ms from tracker")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_battery_level", () -> latestBatteryLevel)
            .description("Latest battery level from tracker")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_pending_sessions", () -> latestPendingSessions)
            .description("Latest pending sessions count from tracker")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_pending_logs", () -> latestPendingLogs)
            .description("Latest pending logs count from tracker")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_is_charging", () -> latestIsCharging ? 1.0 : 0.0)
            .description("Is tracker device charging (1=yes, 0=no)")
            .register(meterRegistry);
        Gauge.builder("lifeos_tracker_is_wifi", () -> latestIsWifi ? 1.0 : 0.0)
            .description("Is tracker device on WiFi (1=yes, 0=no)")
            .register(meterRegistry);
    }

    @Transactional
    public SyncResponse processSync(SyncRequest request) {
        request.getSessions().removeIf(sessionDto -> sessionDto.getStartTime().isAfter(sessionDto.getEndTime()));
        return syncTimer.record(() -> {
            syncCounter.increment();
            String deviceId = request.getDeviceId();
            log.info("Processing sync from device: {}", deviceId);

            int syncedSessions = 0;
            int syncedLogs = 0;

            try {
                // 1. Save sessions (skip duplicates)
                syncedSessions = saveSessions(deviceId, request.getSessions());

                // 2. Save logs
                syncedLogs = saveLogs(deviceId, request.getLogs());

                // 3. Save metrics snapshots
                saveMetrics(deviceId, request.getMetrics());

                // 4. Send Discord alert
                discordAlertService.sendSyncAlert(deviceId, syncedSessions, syncedLogs);

                log.info("Sync completed for device {}: {} sessions, {} logs",
                    deviceId, syncedSessions, syncedLogs);

                return SyncResponse.builder()
                    .success(true)
                    .syncedSessions(syncedSessions)
                    .syncedLogs(syncedLogs)
                    .message("Sync completed successfully")
                    .build();

            } catch (Exception e) {
                syncErrorCounter.increment();
                log.error("Sync failed for device {}: {}", deviceId, e.getMessage(), e);
                throw e;
            }
        });
    }

    private int saveSessions(String deviceId, List<SessionDto> sessionDtos) {
        if (sessionDtos == null || sessionDtos.isEmpty()) {
            return 0;
        }

        List<ActivitySession> toSave = new ArrayList<>();
        int duplicates = 0;

        for (SessionDto dto : sessionDtos) {
            // Check for duplicate using the unique constraint fields
            boolean exists = sessionRepository.existsByDeviceIdAndPackageNameAndStartTime(
                deviceId, dto.getPackageName(), dto.getStartTime()
            );

            if (exists) {
                duplicates++;
                log.debug("Skipping duplicate session: {} / {} / {}",
                    deviceId, dto.getPackageName(), dto.getStartTime());
                continue;
            }

            ActivitySession session = ActivitySession.builder()
                .deviceId(deviceId)
                .source(dto.getSource())
                .type(dto.getType())
                .packageName(dto.getPackageName())
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .durationMillis(dto.getDurationMillis())
                .build();

            toSave.add(session);
        }

        if (!toSave.isEmpty()) {
            sessionRepository.saveAll(toSave);
            sessionsSyncedCounter.increment(toSave.size());
        }

        if (duplicates > 0) {
            duplicateSessionsCounter.increment(duplicates);
            log.info("Skipped {} duplicate sessions for device {}", duplicates, deviceId);
        }

        return toSave.size();
    }

    private int saveLogs(String deviceId, List<TrackerLogDto> logDtos) {
        if (logDtos == null || logDtos.isEmpty()) {
            return 0;
        }

        List<TrackerLog> logs = new ArrayList<>();
        for (TrackerLogDto dto : logDtos) {
            TrackerLog trackerLog = TrackerLog.builder()
                .deviceId(deviceId)
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                .level(dto.getLevel())
                .tag(dto.getTag())
                .eventType(dto.getEventType())
                .message(dto.getMessage())
                .build();

            logs.add(trackerLog);

            // Alert on ERROR logs
            if ("ERROR".equalsIgnoreCase(dto.getLevel())) {
                discordAlertService.sendErrorLogAlert(deviceId, dto.getTag(), dto.getMessage());
            }
        }

        logRepository.saveAll(logs);
        logsSyncedCounter.increment(logs.size());

        return logs.size();
    }

    private void saveMetrics(String deviceId, List<AppMetricsDto> metricsDtos) {
        if (metricsDtos == null || metricsDtos.isEmpty()) {
            return;
        }

        List<AppMetrics> toSave = new ArrayList<>();

        for (AppMetricsDto dto : metricsDtos) {
            AppMetrics metrics = AppMetrics.builder()
                .deviceId(deviceId)
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                .heapUsedMb(dto.getHeapUsedMb())
                .heapTotalMb(dto.getHeapTotalMb())
                .heapMaxMb(dto.getHeapMaxMb())
                .lastScanDurationMs(dto.getLastScanDurationMs())
                .sessionsFoundInScan(dto.getSessionsFoundInScan())
                .pendingSessionsCount(dto.getPendingSessionsCount())
                .pendingLogsCount(dto.getPendingLogsCount())
                .batteryLevel(dto.getBatteryLevel())
                .isCharging(dto.getIsCharging())
                .isWifiConnected(dto.getIsWifiConnected())
                .build();

            toSave.add(metrics);
        }

        metricsRepository.saveAll(toSave);

        // Update Gauges from the latest snapshot (most recent timestamp)
        AppMetricsDto latest = metricsDtos.stream()
            .max((a, b) -> {
                Instant ta = a.getTimestamp() != null ? a.getTimestamp() : Instant.MIN;
                Instant tb = b.getTimestamp() != null ? b.getTimestamp() : Instant.MIN;
                return ta.compareTo(tb);
            })
            .orElse(null);

        if (latest != null) {
            if (latest.getHeapUsedMb() != null) this.latestHeapUsedMb = latest.getHeapUsedMb();
            if (latest.getHeapMaxMb() != null) this.latestHeapMaxMb = latest.getHeapMaxMb();
            if (latest.getLastScanDurationMs() != null) this.latestScanDurationMs = latest.getLastScanDurationMs();
            if (latest.getBatteryLevel() != null) this.latestBatteryLevel = latest.getBatteryLevel();
            if (latest.getPendingSessionsCount() != null) this.latestPendingSessions = latest.getPendingSessionsCount();
            if (latest.getPendingLogsCount() != null) this.latestPendingLogs = latest.getPendingLogsCount();
            if (latest.getIsCharging() != null) this.latestIsCharging = latest.getIsCharging();
            if (latest.getIsWifiConnected() != null) this.latestIsWifi = latest.getIsWifiConnected();
        }

        log.info("Saved {} metrics snapshots for device {}", toSave.size(), deviceId);
    }
}
