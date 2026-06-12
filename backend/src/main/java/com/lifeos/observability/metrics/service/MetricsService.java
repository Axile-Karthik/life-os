package com.lifeos.observability.metrics.service;

import com.lifeos.observability.metrics.entity.AppMetrics;
import java.time.Instant;
import java.util.List;

public interface MetricsService {
    List<AppMetrics> getFilteredMetrics(String deviceId, Instant startDate, Instant endDate);
    AppMetrics getLatestMetrics(String deviceId);
}
