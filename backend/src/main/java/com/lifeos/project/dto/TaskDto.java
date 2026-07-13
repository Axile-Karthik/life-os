package com.lifeos.project.dto;

import com.lifeos.project.enums.TaskPriority;
import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record TaskDto(
    UUID id,
    String ticketKey,
    String title,
    String description,
    UUID columnId,
    Integer position,
    TaskPriority priority,
    Instant dueDate,
    UUID parentId,
    List<TaskDto> subtasks,
    Integer version,
    Set<LabelDto> labels,
    List<CommentDto> comments,
    List<AttachmentDto> attachments,
    Instant createdAt,
    Instant updatedAt
) {}
