package com.lifeos.progression.xp.entity;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.skills.entity.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing the historical log of XP awarded to characters.
 * Never deleted, acts as a ledger.
 */
@Entity
@Table(
    name = "xp_history",
    indexes = {
        @Index(name = "idx_xp_history_char_created", columnList = "character_id, created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XPHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private int xp;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false, length = 1000)
    private String reason;

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
