package com.lifeos.telemetry.session.repository;

import com.lifeos.telemetry.session.entity.ActivitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, Long> {

    List<ActivitySession> findByDeviceId(String deviceId);

    List<ActivitySession> findByType(String type);

    List<ActivitySession> findBySource(String source);

    List<ActivitySession> findByStartTimeBetween(Instant start, Instant end);

    @Query("SELECT a FROM ActivitySession a WHERE " +
           "(cast(:deviceId as string) IS NULL OR a.deviceId = :deviceId) AND " +
           "(cast(:type as string) IS NULL OR a.type = :type) AND " +
           "(cast(:source as string) IS NULL OR a.source = :source) AND " +
           "(cast(:startDate as instant) IS NULL OR a.startTime >= :startDate) AND " +
           "(cast(:endDate as instant) IS NULL OR a.startTime <= :endDate) " +
           "ORDER BY a.startTime DESC")
    List<ActivitySession> findFiltered(
        @Param("deviceId") String deviceId,
        @Param("type") String type,
        @Param("source") String source,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    boolean existsByDeviceIdAndPackageNameAndStartTime(String deviceId, String packageName, Instant startTime);

    @Query("SELECT a.type, SUM(a.durationMillis) FROM ActivitySession a " +
           "WHERE a.startTime >= :startDate AND a.startTime <= :endDate " +
           "GROUP BY a.type")
    List<Object[]> getDurationByType(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT CAST(a.startTime AS date), COUNT(a), SUM(a.durationMillis) " +
           "FROM ActivitySession a " +
           "WHERE (cast(:deviceId as string) IS NULL OR a.deviceId = :deviceId) AND " +
           "a.startTime >= :startDate AND a.startTime <= :endDate " +
           "GROUP BY CAST(a.startTime AS date) " +
           "ORDER BY CAST(a.startTime AS date) DESC")
    List<Object[]> getDailyStats(
        @Param("deviceId") String deviceId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT a.title, a.packageName, COUNT(a), SUM(a.durationMillis) " +
           "FROM ActivitySession a " +
           "WHERE (cast(:deviceId as string) IS NULL OR a.deviceId = :deviceId) AND " +
           "(cast(:type as string) IS NULL OR a.type = :type) AND " +
           "a.startTime >= :startDate AND a.startTime <= :endDate " +
           "GROUP BY a.title, a.packageName " +
           "ORDER BY SUM(a.durationMillis) DESC")
    List<Object[]> getPerGameStats(
        @Param("deviceId") String deviceId,
        @Param("type") String type,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT MAX(a.createdAt) FROM ActivitySession a WHERE " +
           "(cast(:deviceId as string) IS NULL OR a.deviceId = :deviceId)")
    Instant findLastSyncTime(@Param("deviceId") String deviceId);
}
