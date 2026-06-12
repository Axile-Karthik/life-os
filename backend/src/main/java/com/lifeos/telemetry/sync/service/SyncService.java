package com.lifeos.telemetry.sync.service;

import com.lifeos.telemetry.sync.dto.SyncRequest;
import com.lifeos.telemetry.sync.dto.SyncResponse;

public interface SyncService {
    SyncResponse processSync(SyncRequest request);
}
