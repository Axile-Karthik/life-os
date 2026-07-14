package com.lifeos.progression.character.mapper;

import com.lifeos.progression.character.dto.CharacterResponse;
import com.lifeos.progression.character.dto.CharacterStatDto;
import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.entity.CharacterStat;
import com.lifeos.progression.title.mapper.TitleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Character entities to DTOs and vice-versa.
 */
@Component
@RequiredArgsConstructor
public class CharacterMapper {

    private final TitleMapper titleMapper;

    public CharacterResponse toResponse(Character character) {
        if (character == null) {
            return null;
        }

        return CharacterResponse.builder()
                .id(character.getId())
                .userId(character.getUserId())
                .currentLevel(character.getCurrentLevel())
                .totalXp(character.getTotalXp())
                .title(titleMapper.toResponse(character.getTitle()))
                .stats(toStatDto(character.getStats()))
                .createdAt(character.getCreatedAt())
                .updatedAt(character.getUpdatedAt())
                .build();
    }

    public CharacterStatDto toStatDto(CharacterStat stats) {
        if (stats == null) {
            return new CharacterStatDto(10, 10, 10, 10, 10, 10);
        }
        return CharacterStatDto.builder()
                .knowledge(stats.getKnowledge())
                .discipline(stats.getDiscipline())
                .focus(stats.getFocus())
                .endurance(stats.getEndurance())
                .strength(stats.getStrength())
                .creativity(stats.getCreativity())
                .build();
    }

    public CharacterStat toStatEntity(CharacterStatDto dto) {
        if (dto == null) {
            return new CharacterStat(10, 10, 10, 10, 10, 10);
        }
        return CharacterStat.builder()
                .knowledge(dto.getKnowledge())
                .discipline(dto.getDiscipline())
                .focus(dto.getFocus())
                .endurance(dto.getEndurance())
                .strength(dto.getStrength())
                .creativity(dto.getCreativity())
                .build();
    }
}
