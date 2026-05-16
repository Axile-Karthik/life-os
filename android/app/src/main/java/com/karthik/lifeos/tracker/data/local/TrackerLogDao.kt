package com.karthik.lifeos.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackerLogDao {

    @Insert
    suspend fun insert(log: TrackerLog)

    @Insert
    suspend fun insertAll(logs: List<TrackerLog>)

    /** Return unsynced logs, oldest first, capped at 200 per sync batch. */
    @Query("SELECT * FROM tracker_logs WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT 200")
    suspend fun getUnsyncedLogs(): List<TrackerLog>

    /** Mark logs as synced after successful backend POST. */
    @Query("UPDATE tracker_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markLogsSynced(ids: List<Long>)

    /** Count of unsynced logs — used by MetricsCollector. */
    @Query("SELECT COUNT(*) FROM tracker_logs WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int

    /** Delete synced logs older than cutoff — called by CleanupWorker. */
    @Query("DELETE FROM tracker_logs WHERE isSynced = 1 AND timestamp < :cutoff")
    suspend fun deleteOldSyncedLogs(cutoff: Long)

    /** Return all logs (for debugging). */
    @Query("SELECT * FROM tracker_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<TrackerLog>
}
