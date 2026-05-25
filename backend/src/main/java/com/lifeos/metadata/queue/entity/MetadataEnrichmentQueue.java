package com.lifeos.metadata.queue.entity;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.queue.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "metadata_enrichment_queue",
    indexes = {
        @Index(name = "idx_queue_normalized_title", columnList = "normalized_title"),
        @Index(name = "idx_queue_content_type", columnList = "content_type"),
        @Index(name = "idx_queue_status", columnList = "queue_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataEnrichmentQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "raw_title", length = 500)
    private String rawTitle;

    @Column(name = "normalized_title", length = 500)
    private String normalizedTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 50)
    private ContentType contentType;

    @Column(name = "source_platform", length = 100)
    private String sourcePlatform;

    @Column(name = "external_source", length = 100)
    private String externalSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_status", length = 50)
    private QueueStatus queueStatus;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
