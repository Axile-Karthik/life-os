package com.lifeos.observability.metrics.repository;

import com.lifeos.observability.metrics.entity.AppMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppMetricsRepository extends JpaRepository<AppMetrics, Long> {

    @Query("SELECT m FROM AppMetrics m WHERE " +
           "(cast(:deviceId as string) IS NULL OR m.deviceId = :deviceId) AND " +
           "(cast(:startDate as instant) IS NULL OR m.timestamp >= :startDate) AND " +
           "(cast(:endDate as instant) IS NULL OR m.timestamp <= :endDate) " +
           "ORDER BY m.timestamp DESC")
    List<AppMetrics> findFiltered(
        @Param("deviceId") String deviceId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT m FROM AppMetrics m WHERE m.deviceId = :deviceId " +
           "ORDER BY m.timestamp DESC LIMIT 1")
    AppMetrics findLatestByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Query("DELETE FROM AppMetrics m WHERE m.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
