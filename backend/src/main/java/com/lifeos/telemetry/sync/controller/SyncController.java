package com.lifeos.telemetry.sync.controller;

import com.lifeos.telemetry.sync.dto.SyncRequest;
import com.lifeos.telemetry.sync.dto.SyncResponse;
import com.lifeos.telemetry.sync.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> sync(@Valid @RequestBody SyncRequest request) {
        log.info("Received sync request from device: {}", request.getDeviceId());
        SyncResponse response = syncService.processSync(request);
        return ResponseEntity.ok(response);
    }
}
