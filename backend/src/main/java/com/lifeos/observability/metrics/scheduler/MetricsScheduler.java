package com.lifeos.observability.metrics.scheduler;

import com.lifeos.common.service.CleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsScheduler {

    private final CleanupService cleanupService;

    /**
     * Every midnight (server time): cleanup old metrics (30 days)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightMetricsCleanup() {
        log.info("Running midnight scheduled task: cleanup old metrics");
        cleanupService.cleanupOldMetrics();
    }
}
