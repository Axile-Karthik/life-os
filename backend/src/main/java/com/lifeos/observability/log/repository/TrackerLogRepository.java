package com.lifeos.observability.log.repository;

import com.lifeos.observability.log.entity.TrackerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TrackerLogRepository extends JpaRepository<TrackerLog, Long> {

    @Query("SELECT t FROM TrackerLog t WHERE " +
           "(cast(:deviceId as string) IS NULL OR t.deviceId = :deviceId) AND " +
           "(cast(:level as string) IS NULL OR t.level = :level) AND " +
           "(cast(:startDate as instant) IS NULL OR t.timestamp >= :startDate) AND " +
           "(cast(:endDate as instant) IS NULL OR t.timestamp <= :endDate) " +
           "ORDER BY t.timestamp DESC")
    List<TrackerLog> findFiltered(
        @Param("deviceId") String deviceId,
        @Param("level") String level,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    List<TrackerLog> findByLevelOrderByTimestampDesc(String level);

    @Modifying
    @Query("DELETE FROM TrackerLog t WHERE t.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
