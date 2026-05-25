package com.lifeos.metadata.entity;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.enums.MetadataState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "content_metadata",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_metadata_type_title", columnNames = {"content_type", "normalized_title"}),
        @UniqueConstraint(name = "uq_metadata_source_id", columnNames = {"external_source", "external_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "normalized_title", nullable = false, length = 500)
    private String normalizedTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "backdrop_url", columnDefinition = "TEXT")
    private String backdropUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String genres;

    @Column(name = "external_source", length = 100)
    private String externalSource;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_state", nullable = false, length = 50)
    @Builder.Default
    private MetadataState metadataState = MetadataState.PENDING;

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
        if (metadataState == null) {
            metadataState = MetadataState.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
