package com.lifeos.project.mapper;

import com.lifeos.project.dto.BoardColumnDto;
import com.lifeos.project.entity.BoardColumn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class BoardColumnMapper {

    private final TaskMapper taskMapper;

    public BoardColumnMapper(@Lazy TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public BoardColumnDto toDto(BoardColumn entity) {
        if (entity == null) return null;
        return BoardColumnDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .position(entity.getPosition())
                .boardId(entity.getBoard().getId())
                .tasks(entity.getTasks() == null ? null : entity.getTasks().stream()
                        .filter(task -> task.getParent() == null)
                        .map(taskMapper::toDto)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
