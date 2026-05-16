package com.lifeos.common.service;

import com.lifeos.observability.metrics.repository.AppMetricsRepository;
import com.lifeos.observability.log.repository.TrackerLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final TrackerLogRepository logRepository;
    private final AppMetricsRepository metricsRepository;

    @Value("${app.cleanup.logs-retention-days:7}")
    private int logsRetentionDays;

    @Value("${app.cleanup.metrics-retention-days:30}")
    private int metricsRetentionDays;

    @Transactional
    public void cleanupOldLogs() {
        Instant cutoff = Instant.now().minus(logsRetentionDays, ChronoUnit.DAYS);
        int deleted = logRepository.deleteOlderThan(cutoff);
        log.info("Cleanup: deleted {} tracker logs older than {} days", deleted, logsRetentionDays);
    }

    @Transactional
    public void cleanupOldMetrics() {
        Instant cutoff = Instant.now().minus(metricsRetentionDays, ChronoUnit.DAYS);
        int deleted = metricsRepository.deleteOlderThan(cutoff);
        log.info("Cleanup: deleted {} app metrics older than {} days", deleted, metricsRetentionDays);
    }
}
