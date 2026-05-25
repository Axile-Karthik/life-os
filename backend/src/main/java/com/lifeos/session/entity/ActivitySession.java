package com.lifeos.session.entity;

import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.session.enums.SessionState;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "activity_session",
    indexes = {
        @Index(name = "idx_session_metadata_id", columnList = "metadata_id"),
        @Index(name = "idx_session_package_name", columnList = "package_name"),
        @Index(name = "idx_session_normalized_title", columnList = "normalized_title"),
        @Index(name = "idx_session_content_type", columnList = "content_type"),
        @Index(name = "idx_session_started_at", columnList = "started_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metadata_id")
    private ContentMetadata metadata;

    @Column(name = "package_name", length = 500)
    private String packageName;

    @Column(name = "raw_title", length = 500)
    private String rawTitle;

    @Column(name = "normalized_title", length = 500)
    private String normalizedTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 50)
    private ContentType contentType;

    @Column(name = "source_platform", length = 100)
    private String sourcePlatform;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_state", length = 50)
    private SessionState sessionState;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
