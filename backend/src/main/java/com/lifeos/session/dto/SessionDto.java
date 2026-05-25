package com.lifeos.session.dto;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.session.enums.SessionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private UUID id;
    private UUID metadataId;
    private String packageName;
    private String rawTitle;
    private String normalizedTitle;
    private ContentType contentType;
    private String sourcePlatform;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationSeconds;
    private SessionState sessionState;
    
    // Joined metadata information if resolved
    private String resolvedTitle;
    private String imageUrl;
}
