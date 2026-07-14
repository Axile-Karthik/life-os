package com.lifeos.progression.character.entity;

import com.lifeos.progression.title.entity.Title;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a Character in Life OS progression system.
 */
@Entity
@Table(
    name = "character",
    indexes = {
        @Index(name = "idx_character_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private int currentLevel = 1;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private int totalXp = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id")
    private Title title;

    @Embedded
    @Builder.Default
    private CharacterStat stats = new CharacterStat();

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
        if (stats == null) {
            stats = new CharacterStat();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
