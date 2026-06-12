package com.lifeos.session.service;

import com.lifeos.session.dto.CreateSessionRequest;
import com.lifeos.session.entity.ActivitySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface SessionService {
    ActivitySession saveSession(CreateSessionRequest request);
    List<ActivitySession> getFilteredSessions(String sourcePlatform, String typeStr, String source, Instant startDate, Instant endDate);
    Page<ActivitySession> getPageableSessions(String typeStr, Instant startDate, Instant endDate, Pageable pageable);
    List<Map<String, Object>> getDailyStats(String sourcePlatform, Instant startDate, Instant endDate);
    List<Map<String, Object>> getWeeklyStats(String sourcePlatform, Instant startDate, Instant endDate);
    List<Map<String, Object>> getPerGameStats(String sourcePlatform, String typeStr, Instant startDate, Instant endDate);
    Map<String, Long> getDurationByType(Instant startDate, Instant endDate);
    Instant getLastSyncTime(String sourcePlatform);

    static String formatDuration(long millis) {
        long totalMinutes = millis / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
