package com.lifeos.telemetry.session.service;

import com.lifeos.telemetry.session.entity.ActivitySession;
import com.lifeos.telemetry.session.repository.ActivitySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final ActivitySessionRepository sessionRepository;

    public List<ActivitySession> getFilteredSessions(String deviceId, String type, String source,
                                                      Instant startDate, Instant endDate) {
        return sessionRepository.findFiltered(deviceId, type, source, startDate, endDate);
    }

    public List<Map<String, Object>> getDailyStats(String deviceId, Instant startDate, Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().atOffset(ZoneOffset.UTC).minusDays(30).toInstant();
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        List<Object[]> rows = sessionRepository.getDailyStats(deviceId, startDate, endDate);
        return rows.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", row[0] != null ? row[0].toString() : null);
            entry.put("sessionCount", row[1]);
            entry.put("totalDurationMillis", row[2]);
            entry.put("totalDurationFormatted", formatDuration((Long) row[2]));
            return entry;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getWeeklyStats(String deviceId, Instant startDate, Instant endDate) {
        // Aggregate daily stats into weekly buckets
        if (startDate == null) {
            startDate = Instant.now().atOffset(ZoneOffset.UTC).minusDays(90).toInstant();
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        List<ActivitySession> sessions = sessionRepository.findFiltered(deviceId, null, null, startDate, endDate);

        // Group by ISO week
        Map<String, long[]> weeklyMap = new LinkedHashMap<>();
        for (ActivitySession s : sessions) {
            LocalDate date = s.getStartTime().atOffset(ZoneOffset.UTC).toLocalDate();
            // Week key: year-Wxx
            int weekOfYear = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
            int year = date.get(java.time.temporal.WeekFields.ISO.weekBasedYear());
            String weekKey = year + "-W" + String.format("%02d", weekOfYear);

            weeklyMap.computeIfAbsent(weekKey, k -> new long[]{0, 0});
            weeklyMap.get(weekKey)[0]++; // count
            weeklyMap.get(weekKey)[1] += s.getDurationMillis(); // duration
        }

        return weeklyMap.entrySet().stream().map(e -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("week", e.getKey());
            entry.put("sessionCount", e.getValue()[0]);
            entry.put("totalDurationMillis", e.getValue()[1]);
            entry.put("totalDurationFormatted", formatDuration(e.getValue()[1]));
            return entry;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPerGameStats(String deviceId, String type,
                                                      Instant startDate, Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().atOffset(ZoneOffset.UTC).minusDays(30).toInstant();
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        List<Object[]> rows = sessionRepository.getPerGameStats(deviceId, type, startDate, endDate);
        return rows.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("title", row[0]);
            entry.put("packageName", row[1]);
            entry.put("sessionCount", row[2]);
            entry.put("totalDurationMillis", row[3]);
            entry.put("totalDurationFormatted", formatDuration((Long) row[3]));
            return entry;
        }).collect(Collectors.toList());
    }

    /**
     * Get a summary of total duration by activity type for a given date range.
     * Used for Discord daily summaries.
     */
    public Map<String, Long> getDurationByType(Instant startDate, Instant endDate) {
        List<Object[]> rows = sessionRepository.getDurationByType(startDate, endDate);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    public Instant getLastSyncTime(String deviceId) {
        return sessionRepository.findLastSyncTime(deviceId);
    }

    public static String formatDuration(long millis) {
        long totalMinutes = millis / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
