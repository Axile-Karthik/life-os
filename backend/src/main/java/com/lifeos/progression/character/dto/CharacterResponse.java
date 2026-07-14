package com.lifeos.progression.character.dto;

import com.lifeos.progression.title.dto.TitleResponse;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterResponse {
    private Long id;
    private UUID userId;
    private int currentLevel;
    private int totalXp;
    private TitleResponse title;
    private CharacterStatDto stats;
    private Instant createdAt;
    private Instant updatedAt;
}
