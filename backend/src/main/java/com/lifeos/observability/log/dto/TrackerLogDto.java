package com.lifeos.observability.log.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerLogDto {

    private Instant timestamp;

    private String level;

    private String tag;

    private String eventType;

    private String message;
}
