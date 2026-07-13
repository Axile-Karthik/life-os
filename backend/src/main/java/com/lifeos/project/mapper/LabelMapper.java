package com.lifeos.project.mapper;

import com.lifeos.project.dto.LabelDto;
import com.lifeos.project.entity.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {

    public LabelDto toDto(Label entity) {
        if (entity == null) return null;
        return LabelDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .color(entity.getColor())
                .build();
    }
}
