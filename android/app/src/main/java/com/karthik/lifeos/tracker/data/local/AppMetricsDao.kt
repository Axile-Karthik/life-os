package com.karthik.lifeos.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppMetricsDao {

    @Insert
    suspend fun insert(metrics: AppMetrics)

    /** Return unsynced metrics snapshots, oldest first. */
    @Query("SELECT * FROM app_metrics WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedMetrics(): List<AppMetrics>

    /** Mark metrics as synced after successful backend POST. */
    @Query("UPDATE app_metrics SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markMetricsSynced(ids: List<Long>)

    /** Count of unsynced metrics — used by MetricsCollector itself. */
    @Query("SELECT COUNT(*) FROM app_metrics WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int

    /** Delete synced metrics older than cutoff — called by CleanupWorker. */
    @Query("DELETE FROM app_metrics WHERE isSynced = 1 AND timestamp < :cutoff")
    suspend fun deleteOldSyncedMetrics(cutoff: Long)
}
