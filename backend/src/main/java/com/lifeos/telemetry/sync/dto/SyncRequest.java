package com.lifeos.telemetry.sync.dto;
import com.lifeos.observability.metrics.dto.AppMetricsDto;
import com.lifeos.telemetry.sync.dto.SyncSessionDto;

import com.lifeos.observability.log.dto.TrackerLogDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRequest {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    private List<SyncSessionDto> sessions;

    private List<TrackerLogDto> logs;

    private List<AppMetricsDto> metrics;
}
