package com.lifeos.project.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record BoardColumnDto(
    UUID id,
    String name,
    Integer position,
    UUID boardId,
    List<TaskDto> tasks,
    Instant createdAt,
    Instant updatedAt
) {}
