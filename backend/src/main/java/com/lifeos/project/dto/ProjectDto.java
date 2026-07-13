package com.lifeos.project.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ProjectDto(
    UUID id,
    String name,
    String description,
    String ticketPrefix,
    Integer version,
    Instant createdAt,
    Instant updatedAt
) {}
