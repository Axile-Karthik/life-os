package com.lifeos.progression.character.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Value object representing a character's core RPG stats.
 * Embedded directly within the Character entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterStat {

    @Builder.Default
    private int knowledge = 10;

    @Builder.Default
    private int discipline = 10;

    @Builder.Default
    private int focus = 10;

    @Builder.Default
    private int endurance = 10;

    @Builder.Default
    private int strength = 10;

    @Builder.Default
    private int creativity = 10;
}
