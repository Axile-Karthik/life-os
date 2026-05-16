package com.lifeos.observability.metrics.service;

import com.lifeos.observability.metrics.entity.AppMetrics;
import com.lifeos.observability.metrics.repository.AppMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final AppMetricsRepository metricsRepository;

    public List<AppMetrics> getFilteredMetrics(String deviceId, Instant startDate, Instant endDate) {
        return metricsRepository.findFiltered(deviceId, startDate, endDate);
    }

    public AppMetrics getLatestMetrics(String deviceId) {
        return metricsRepository.findLatestByDeviceId(deviceId);
    }
}
