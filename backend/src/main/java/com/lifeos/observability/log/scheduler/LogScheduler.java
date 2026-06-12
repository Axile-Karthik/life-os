package com.lifeos.observability.log.scheduler;

import com.lifeos.shared.service.CleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogScheduler {

    private final CleanupService cleanupService;

    /**
     * Every midnight (server time): cleanup old logs (7 days)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightLogCleanup() {
        log.info("Running midnight scheduled task: cleanup old logs");
        cleanupService.cleanupOldLogs();
    }
}
