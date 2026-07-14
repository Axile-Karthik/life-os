package com.lifeos.progression.title.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an unlockable RPG title for a character.
 */
@Entity
@Table(name = "title")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    private String icon;

    @Column(name = "required_level", nullable = false)
    @Builder.Default
    private int requiredLevel = 1;
}
