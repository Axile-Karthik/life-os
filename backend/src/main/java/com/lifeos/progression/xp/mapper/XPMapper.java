package com.lifeos.progression.xp.mapper;

import com.lifeos.progression.xp.dto.XPHistoryResponse;
import com.lifeos.progression.xp.entity.XPHistory;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting XP-related entities to DTOs.
 */
@Component
public class XPMapper {

    public XPHistoryResponse toResponse(XPHistory history) {
        if (history == null) {
            return null;
        }
        return XPHistoryResponse.builder()
                .id(history.getId())
                .characterId(history.getCharacter() != null ? history.getCharacter().getId() : null)
                .skillId(history.getSkill() != null ? history.getSkill().getId() : null)
                .skillName(history.getSkill() != null ? history.getSkill().getName() : null)
                .xp(history.getXp())
                .source(history.getSource())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
