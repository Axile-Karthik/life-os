package com.lifeos.project.dto;

import com.lifeos.project.enums.ActivityAction;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ActivityLogDto(
    UUID id,
    UUID projectId,
    UUID boardId,
    UUID taskId,
    String taskTitle,
    ActivityAction actionType,
    String details,
    Instant createdAt
) {}
