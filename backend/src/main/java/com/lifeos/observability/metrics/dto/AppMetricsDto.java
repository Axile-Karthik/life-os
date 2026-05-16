package com.lifeos.observability.metrics.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMetricsDto {

    private Instant timestamp;

    private Double heapUsedMb;

    private Double heapTotalMb;

    private Double heapMaxMb;

    private Long lastScanDurationMs;

    private Integer sessionsFoundInScan;

    private Integer pendingSessionsCount;

    private Integer pendingLogsCount;

    private Integer batteryLevel;

    private Boolean isCharging;

    private Boolean isWifiConnected;
}
