package com.lifeos.project.mapper;

import com.lifeos.project.dto.TaskDto;
import com.lifeos.project.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final LabelMapper labelMapper;
    private final CommentMapper commentMapper;
    private final AttachmentMapper attachmentMapper;

    public TaskDto toDto(Task entity) {
        if (entity == null) return null;
        return TaskDto.builder()
                .id(entity.getId())
                .ticketKey(entity.getTicketKey())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .columnId(entity.getColumn().getId())
                .position(entity.getPosition())
                .priority(entity.getPriority())
                .dueDate(entity.getDueDate())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .subtasks(entity.getSubtasks() == null ? null : entity.getSubtasks().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()))
                .version(entity.getVersion())
                .labels(entity.getLabels() == null ? null : entity.getLabels().stream()
                        .map(labelMapper::toDto)
                        .collect(Collectors.toSet()))
                .comments(entity.getComments() == null ? null : entity.getComments().stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()))
                .attachments(entity.getAttachments() == null ? null : entity.getAttachments().stream()
                        .map(attachmentMapper::toDto)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
