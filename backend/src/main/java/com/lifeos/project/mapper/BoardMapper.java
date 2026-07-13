package com.lifeos.project.mapper;

import com.lifeos.project.dto.BoardDto;
import com.lifeos.project.entity.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BoardMapper {

    private final BoardColumnMapper columnMapper;

    public BoardDto toDto(Board entity) {
        if (entity == null) return null;
        return BoardDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .projectId(entity.getProject().getId())
                .columns(entity.getColumns() == null ? null : entity.getColumns().stream()
                        .map(columnMapper::toDto)
                        .collect(Collectors.toList()))
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
