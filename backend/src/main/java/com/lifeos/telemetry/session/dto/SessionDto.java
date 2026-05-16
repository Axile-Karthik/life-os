package com.lifeos.telemetry.session.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {

    @NotBlank(message = "source is required")
    private String source;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "packageName is required")
    private String packageName;

    @NotBlank(message = "title is required")
    private String title;

    private Instant startTime;

    private Instant endTime;

    private Long durationMillis;
}
