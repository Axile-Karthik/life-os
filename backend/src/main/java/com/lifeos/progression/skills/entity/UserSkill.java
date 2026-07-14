package com.lifeos.progression.skills.entity;

import com.lifeos.progression.character.entity.Character;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing a character's level and experience progress in a specific skill.
 */
@Entity
@Table(
    name = "user_skill",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_character_skill", columnNames = {"character_id", "skill_id"})
    },
    indexes = {
        @Index(name = "idx_user_skill_char_skill", columnList = "character_id, skill_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private int currentLevel = 1;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private int totalXp = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean unlocked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
