package com.lifeos.progression.character.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsUpdateRequest {

    @NotNull(message = "Character ID is required")
    private Long characterId;

    @NotNull(message = "Stats details are required")
    @Valid
    private CharacterStatDto stats;
}
