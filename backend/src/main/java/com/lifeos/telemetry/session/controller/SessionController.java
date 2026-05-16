package com.lifeos.telemetry.session.controller;

import com.lifeos.telemetry.session.entity.ActivitySession;
import com.lifeos.telemetry.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<ActivitySession>> getSessions(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(
            sessionService.getFilteredSessions(deviceId, type, source, startDate, endDate)
        );
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyStats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(sessionService.getDailyStats(deviceId, startDate, endDate));
    }

    @GetMapping("/stats/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyStats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(sessionService.getWeeklyStats(deviceId, startDate, endDate));
    }

    @GetMapping("/stats/pergame")
    public ResponseEntity<List<Map<String, Object>>> getPerGameStats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(sessionService.getPerGameStats(deviceId, type, startDate, endDate));
    }
}
