package com.lifeos.session.service.impl;

import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.queue.service.MetadataEnrichmentQueueService;
import com.lifeos.metadata.repository.ContentMetadataRepository;
import com.lifeos.metadata.service.MetadataNormalizationService;
import com.lifeos.session.dto.CreateSessionRequest;
import com.lifeos.session.entity.ActivitySession;
import com.lifeos.session.mapper.SessionMapper;
import com.lifeos.session.repository.ActivitySessionRepository;
import com.lifeos.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ActivitySessionRepository sessionRepository;
    private final ContentMetadataRepository contentMetadataRepository;
    private final MetadataEnrichmentQueueService queueService;
    private final MetadataNormalizationService normalizationService;
    private final SessionMapper sessionMapper;

    @Override
    @Transactional
    public ActivitySession saveSession(CreateSessionRequest request) {
        log.info("Ingesting telemetry session for title: {}", request.getRawTitle());

        ActivitySession session = sessionMapper.toEntity(request);

        if (session.getStartedAt() == null) {
            session.setStartedAt(Instant.now());
        }

        if (session.getDurationSeconds() == null && session.getStartedAt() != null && session.getEndedAt() != null) {
            session.setDurationSeconds(Duration.between(session.getStartedAt(), session.getEndedAt()).toSeconds());
        }

        // 1. Normalize Title
        String normTitle = normalizationService.normalizeTitle(request.getRawTitle());
        session.setNormalizedTitle(normTitle);

        // 2. Local metadata lookup using normalized title and content type
        if (session.getContentType() != null && !normTitle.isEmpty()) {
            List<ContentMetadata> matches = contentMetadataRepository.findByNormalizedTitleAndContentType(normTitle, session.getContentType());
            if (!matches.isEmpty()) {
                session.setMetadata(matches.getFirst());
                log.info("Linked session to existing metadata: {}", matches.get(0).getTitle());
            } else {
                // Metadata missing - link as null and enqueue metadata enrichment job
                enqueueEnrichment(request.getRawTitle(), session.getContentType(), session.getSourcePlatform());
            }
        } else if (!normTitle.isEmpty()) {
            // If content type is missing, try lookup with GAME first, then other types, or enqueue default GAME enrichment
            List<ContentMetadata> matches = contentMetadataRepository.findByNormalizedTitleAndContentType(normTitle, ContentType.GAME);
            if (!matches.isEmpty()) {
                session.setMetadata(matches.get(0));
                session.setContentType(ContentType.GAME);
            } else {
                enqueueEnrichment(request.getRawTitle(), ContentType.GAME, session.getSourcePlatform());
            }
        }

        return sessionRepository.save(session);
    }

    private void enqueueEnrichment(String rawTitle, ContentType type, String sourcePlatform) {
        log.info("Metadata missing for title '{}'. Enqueuing enrichment job...", rawTitle);
        queueService.enqueueJob(rawTitle, type != null ? type : ContentType.GAME, sourcePlatform, null);
    }

    @Override
    public List<ActivitySession> getFilteredSessions(String sourcePlatform, String typeStr, String source,
                                                      Instant startDate, Instant endDate) {
        ContentType type = null;
        if (typeStr != null) {
            try {
                type = ContentType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        // If sourcePlatform is null but source is provided, fall back to source
        String platform = sourcePlatform != null ? sourcePlatform : source;

        return sessionRepository.findFiltered(platform, type, startDate, endDate);
    }

    @Override
    public Page<ActivitySession> getPageableSessions(String typeStr, Instant startDate, Instant endDate, Pageable pageable) {
        ContentType type = null;
        if (typeStr != null) {
            try {
                type = ContentType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        return sessionRepository.findPageable(type, startDate, endDate, pageable);
    }

    @Override
    public List<Map<String, Object>> getDailyStats(String sourcePlatform, Instant startDate, Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().atOffset(ZoneOffset.UTC).minusDays(30).toInstant();
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        List<Object[]> rows = sessionRepository.getDailyStats(sourcePlatform, startDate, endDate);
        return rows.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", row[0] != null ? row[0].toString() : null);
            entry.put("sessionCount", row[1]);
            entry.put("totalDurationMillis", row[2]);
            entry.put("totalDurationFormatted", formatDuration((Long) row[2]));
            return entry;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getWeeklyStats(String sourcePlatform, Instant startDate, Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().atOffset(ZoneOffset.UTC).minusDays(90).toInstant();
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        List<ActivitySession> sessions = sessionRepository.findFiltered(sourcePlatform, null, startDate, endDate);

        Map<String, long[]> weeklyMap = new LinkedHashMap<>();
        for (ActivitySession s : sessions) {
            LocalDate date = s.getStartedAt().atOffset(ZoneOffset.UTC).toLocalDate();
            int weekOfYear = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
            int year = date.get(java.time.temporal.WeekFields.ISO.weekBasedYear());
            String weekKey = year + "-W" + String.format("%02d", weekOfYear);

            weeklyMap.computeIfAbsent(weekKey, k -> new long[]{0, 0});
            weeklyMap.get(weekKey)[0]++;
            long durationMillis = s.getDurationSeconds() != null ? s.getDurationSeconds() * 1000 : 0;
            weeklyMap.get(weekKey)[1] += durationMillis;
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

    @Override
    public List<Map<String, Object>> getPerGameStats(String sourcePlatform, String typeStr,
                                                      Instant startDate, Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().atOffset(ZoneOffset.UTC).minusDays(30).toInstant();
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        ContentType type = null;
        if (typeStr != null) {
            try {
                type = ContentType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        List<Object[]> rows = sessionRepository.getPerGameStats(sourcePlatform, type, startDate, endDate);
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

    @Override
    public Map<String, Long> getDurationByType(Instant startDate, Instant endDate) {
        List<Object[]> rows = sessionRepository.getDurationByType(startDate, endDate);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String typeStr = row[0] != null ? row[0].toString() : "UNKNOWN";
            result.put(typeStr, (Long) row[1]);
        }
        return result;
    }

    @Override
    public Instant getLastSyncTime(String sourcePlatform) {
        return sessionRepository.findLastSyncTime(sourcePlatform);
    }

    private String formatDuration(long millis) {
        long totalMinutes = millis / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
