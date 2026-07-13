package com.lifeos.project.mapper;

import com.lifeos.project.dto.CommentDto;
import com.lifeos.project.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment entity) {
        if (entity == null) return null;
        return CommentDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
