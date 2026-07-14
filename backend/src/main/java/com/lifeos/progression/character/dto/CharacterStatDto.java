package com.lifeos.progression.character.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterStatDto {

    @Min(value = 0, message = "Knowledge cannot be negative")
    private int knowledge;

    @Min(value = 0, message = "Discipline cannot be negative")
    private int discipline;

    @Min(value = 0, message = "Focus cannot be negative")
    private int focus;

    @Min(value = 0, message = "Endurance cannot be negative")
    private int endurance;

    @Min(value = 0, message = "Strength cannot be negative")
    private int strength;

    @Min(value = 0, message = "Creativity cannot be negative")
    private int creativity;
}
