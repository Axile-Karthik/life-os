package com.lifeos.project.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record BoardDto(
    UUID id,
    String name,
    String description,
    UUID projectId,
    List<BoardColumnDto> columns,
    Integer version,
    Instant createdAt,
    Instant updatedAt
) {}
