package com.lifeos.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    private String packageName;
    private String rawTitle;
    private String contentType;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationSeconds;
    private String sourcePlatform;
    private String sessionState;
}
