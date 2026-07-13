package com.lifeos.project.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record AttachmentDto(
    UUID id,
    String fileName,
    String fileType,
    Long fileSize,
    String filePath,
    Instant createdAt
) {}
