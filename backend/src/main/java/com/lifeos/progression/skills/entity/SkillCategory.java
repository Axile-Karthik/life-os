package com.lifeos.progression.skills.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a high-level category of skills (e.g., Backend, Learning).
 */
@Entity
@Table(name = "skill_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;
}
