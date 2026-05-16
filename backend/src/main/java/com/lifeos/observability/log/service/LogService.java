package com.lifeos.observability.log.service;

import com.lifeos.observability.log.entity.TrackerLog;

import java.time.Instant;
import java.util.List;

public interface LogService {

    List<TrackerLog> getFilteredLogs(String deviceId, String level,
                                     Instant startDate, Instant endDate);
}
