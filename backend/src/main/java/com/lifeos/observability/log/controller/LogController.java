package com.lifeos.observability.log.controller;

import com.lifeos.observability.log.entity.TrackerLog;
import com.lifeos.observability.log.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    public ResponseEntity<List<TrackerLog>> getLogs(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(logService.getFilteredLogs(deviceId, level, startDate, endDate));
    }
}
