package com.lifeos.observability.metrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "app_metrics",
    indexes = {
        @Index(name = "idx_metrics_device_id", columnList = "device_id"),
        @Index(name = "idx_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_metrics_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "heap_used_mb")
    private Double heapUsedMb;

    @Column(name = "heap_total_mb")
    private Double heapTotalMb;

    @Column(name = "heap_max_mb")
    private Double heapMaxMb;

    @Column(name = "last_scan_duration_ms")
    private Long lastScanDurationMs;

    @Column(name = "sessions_found_in_scan")
    private Integer sessionsFoundInScan;

    @Column(name = "pending_sessions_count")
    private Integer pendingSessionsCount;

    @Column(name = "pending_logs_count")
    private Integer pendingLogsCount;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "is_charging")
    private Boolean isCharging;

    @Column(name = "is_wifi_connected")
    private Boolean isWifiConnected;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
