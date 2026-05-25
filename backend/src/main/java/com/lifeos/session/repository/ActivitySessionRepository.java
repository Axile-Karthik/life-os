package com.lifeos.session.repository;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.session.entity.ActivitySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, UUID> {

    List<ActivitySession> findBySourcePlatform(String sourcePlatform);

    List<ActivitySession> findByContentType(ContentType contentType);

    List<ActivitySession> findByMetadataIsNull();

    List<ActivitySession> findByStartedAtBetween(Instant start, Instant end);

    @Query("SELECT a FROM ActivitySession a WHERE " +
           "(cast(:sourcePlatform as string) IS NULL OR a.sourcePlatform = :sourcePlatform) AND " +
           "(:contentType IS NULL OR a.contentType = :contentType) AND " +
           "(cast(:startDate as instant) IS NULL OR a.startedAt >= :startDate) AND " +
           "(cast(:endDate as instant) IS NULL OR a.startedAt <= :endDate) " +
           "ORDER BY a.startedAt DESC")
    List<ActivitySession> findFiltered(
        @Param("sourcePlatform") String sourcePlatform,
        @Param("contentType") ContentType contentType,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT a FROM ActivitySession a WHERE " +
           "(:contentType IS NULL OR a.contentType = :contentType) AND " +
           "(cast(:startDate as instant) IS NULL OR a.startedAt >= :startDate) AND " +
           "(cast(:endDate as instant) IS NULL OR a.startedAt <= :endDate) " +
           "ORDER BY a.startedAt DESC")
    Page<ActivitySession> findPageable(
        @Param("contentType") ContentType contentType,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );

    boolean existsBySourcePlatformAndPackageNameAndStartedAt(String sourcePlatform, String packageName, Instant startedAt);

    @Query("SELECT a.contentType, SUM(a.durationSeconds * 1000) FROM ActivitySession a " +
           "WHERE a.startedAt >= :startDate AND a.startedAt <= :endDate " +
           "GROUP BY a.contentType")
    List<Object[]> getDurationByType(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT CAST(a.startedAt AS date), COUNT(a), SUM(a.durationSeconds * 1000) " +
           "FROM ActivitySession a " +
           "WHERE (cast(:sourcePlatform as string) IS NULL OR a.sourcePlatform = :sourcePlatform) AND " +
           "a.startedAt >= :startDate AND a.startedAt <= :endDate " +
           "GROUP BY CAST(a.startedAt AS date) " +
           "ORDER BY CAST(a.startedAt AS date) DESC")
    List<Object[]> getDailyStats(
        @Param("sourcePlatform") String sourcePlatform,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT a.rawTitle, a.packageName, COUNT(a), SUM(a.durationSeconds * 1000) " +
           "FROM ActivitySession a " +
           "WHERE (cast(:sourcePlatform as string) IS NULL OR a.sourcePlatform = :sourcePlatform) AND " +
           "(:contentType IS NULL OR a.contentType = :contentType) AND " +
           "a.startedAt >= :startDate AND a.startedAt <= :endDate " +
           "GROUP BY a.rawTitle, a.packageName " +
           "ORDER BY SUM(a.durationSeconds) DESC")
    List<Object[]> getPerGameStats(
        @Param("sourcePlatform") String sourcePlatform,
        @Param("contentType") ContentType contentType,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT MAX(a.createdAt) FROM ActivitySession a WHERE " +
           "(cast(:sourcePlatform as string) IS NULL OR a.sourcePlatform = :sourcePlatform)")
    Instant findLastSyncTime(@Param("sourcePlatform") String sourcePlatform);
}
