package com.lifeos.observability.log.service.impl;

import com.lifeos.observability.log.entity.TrackerLog;
import com.lifeos.observability.log.service.LogService;
import com.lifeos.observability.log.repository.TrackerLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final TrackerLogRepository logRepository;

    public List<TrackerLog> getFilteredLogs(String deviceId, String level,
                                            Instant startDate, Instant endDate) {
        return logRepository.findFiltered(deviceId, level, startDate, endDate);
    }
}
