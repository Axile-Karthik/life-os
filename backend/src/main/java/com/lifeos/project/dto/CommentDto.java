package com.lifeos.project.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CommentDto(
    UUID id,
    String content,
    Instant createdAt,
    Instant updatedAt
) {}
