package com.lifeos.session.controller;

import com.lifeos.session.dto.CreateSessionRequest;
import com.lifeos.session.dto.SessionDto;
import com.lifeos.session.entity.ActivitySession;
import com.lifeos.session.mapper.SessionMapper;
import com.lifeos.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SessionMapper sessionMapper;

    @PostMapping
    public ResponseEntity<SessionDto> createSession(@RequestBody CreateSessionRequest request) {
        ActivitySession saved = sessionService.saveSession(request);
        return ResponseEntity.ok(sessionMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<?> getSessions(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        String filterType = contentType != null ? contentType : type;
        Instant filterStart = from != null ? from : startDate;
        Instant filterEnd = to != null ? to : endDate;
        String filterPlatform = sourcePlatform != null ? sourcePlatform : (deviceId != null ? deviceId : source);

        if (page != null) {
            int pageSize = size != null ? size : 50;
            Page<ActivitySession> paged = sessionService.getPageableSessions(filterType, filterStart, filterEnd, PageRequest.of(page, pageSize));
            Page<SessionDto> dtoPage = paged.map(sessionMapper::toDto);
            return ResponseEntity.ok(dtoPage);
        }

        List<ActivitySession> list = sessionService.getFilteredSessions(filterPlatform, filterType, filterPlatform, filterStart, filterEnd);
        List<SessionDto> dtos = list.stream()
                .map(sessionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyStats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) Instant to) {
        String filterPlatform = sourcePlatform != null ? sourcePlatform : deviceId;
        Instant filterStart = from != null ? from : startDate;
        Instant filterEnd = to != null ? to : endDate;
        return ResponseEntity.ok(sessionService.getDailyStats(filterPlatform, filterStart, filterEnd));
    }

    @GetMapping("/stats/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyStats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) Instant to) {
        String filterPlatform = sourcePlatform != null ? sourcePlatform : deviceId;
        Instant filterStart = from != null ? from : startDate;
        Instant filterEnd = to != null ? to : endDate;
        return ResponseEntity.ok(sessionService.getWeeklyStats(filterPlatform, filterStart, filterEnd));
    }

    @GetMapping("/stats/pergame")
    public ResponseEntity<List<Map<String, Object>>> getPerGameStats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String sourcePlatform,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) Instant to) {
        String filterPlatform = sourcePlatform != null ? sourcePlatform : deviceId;
        String filterType = contentType != null ? contentType : type;
        Instant filterStart = from != null ? from : startDate;
        Instant filterEnd = to != null ? to : endDate;
        return ResponseEntity.ok(sessionService.getPerGameStats(filterPlatform, filterType, filterStart, filterEnd));
    }
}
