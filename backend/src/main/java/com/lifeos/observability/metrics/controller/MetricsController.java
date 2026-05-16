package com.lifeos.observability.metrics.controller;

import com.lifeos.observability.metrics.entity.AppMetrics;
import com.lifeos.observability.metrics.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/metrics")
    public ResponseEntity<List<AppMetrics>> getMetrics(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        return ResponseEntity.ok(metricsService.getFilteredMetrics(deviceId, startDate, endDate));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("service", "Life OS Backend");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
}
