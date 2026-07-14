package com.lifeos.progression.title.mapper;

import com.lifeos.progression.title.dto.TitleResponse;
import com.lifeos.progression.title.entity.Title;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Title entities to DTOs.
 */
@Component
public class TitleMapper {

    public TitleResponse toResponse(Title title) {
        if (title == null) {
            return null;
        }
        return TitleResponse.builder()
                .id(title.getId())
                .name(title.getName())
                .description(title.getDescription())
                .icon(title.getIcon())
                .requiredLevel(title.getRequiredLevel())
                .build();
    }
}
