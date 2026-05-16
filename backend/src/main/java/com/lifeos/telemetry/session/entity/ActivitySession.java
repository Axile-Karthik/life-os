package com.lifeos.telemetry.session.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import com.lifeos.metadata.entity.ContentMetadata;

@Entity
@Table(
    name = "activity_session",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_session_device_package_start",
            columnNames = {"device_id", "package_name", "start_time"}
        )
    },
    indexes = {
        @Index(name = "idx_session_device_id", columnList = "device_id"),
        @Index(name = "idx_session_type", columnList = "type"),
        @Index(name = "idx_session_source", columnList = "source"),
        @Index(name = "idx_session_start_time", columnList = "start_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private ContentMetadata content;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String type;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "duration_millis", nullable = false)
    private Long durationMillis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
