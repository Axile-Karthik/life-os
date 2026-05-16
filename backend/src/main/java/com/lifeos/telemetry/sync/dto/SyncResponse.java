package com.lifeos.telemetry.sync.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncResponse {

    private boolean success;

    private int syncedSessions;

    private int syncedLogs;

    private String message;
}
