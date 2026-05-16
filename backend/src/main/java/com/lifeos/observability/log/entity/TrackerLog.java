package com.lifeos.observability.log.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "tracker_log",
    indexes = {
        @Index(name = "idx_log_device_id", columnList = "device_id"),
        @Index(name = "idx_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_log_level", columnList = "level"),
        @Index(name = "idx_log_event_type", columnList = "event_type"),
        @Index(name = "idx_log_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(nullable = false)
    private String tag;

    @Column(name = "event_type")
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
