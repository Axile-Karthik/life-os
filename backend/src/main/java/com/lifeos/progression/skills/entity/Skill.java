package com.lifeos.progression.skills.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a specific skill (e.g. Java, Docker).
 */
@Entity
@Table(
    name = "skill",
    indexes = {
        @Index(name = "idx_skill_category_id", columnList = "category_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private SkillCategory category;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    private String icon;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
