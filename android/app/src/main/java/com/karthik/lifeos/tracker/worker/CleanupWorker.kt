package com.karthik.lifeos.tracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karthik.lifeos.tracker.data.local.AppDatabase
import com.karthik.lifeos.tracker.logging.TrackerLogger

/**
 * Periodic cleanup worker — runs every 24 hours.
 *
 * Responsibilities:
 * - Delete synced logs older than 1 day
 * - Delete synced metrics older than 7 days
 * - Mark stale open sessions (open > 6 hours) as STALE
 *
 * NEVER deletes ActivitySession rows — they remain permanently
 * for offline analytics and future timeline replay.
 */
class CleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "lifeos_cleanup"
        private const val TAG = "CleanupWorker"

        /** Synced logs older than 1 day are deleted. */
        private const val LOG_RETENTION_MS = 24 * 60 * 60 * 1000L

        /** Synced metrics older than 1 days are deleted. */
        private const val METRICS_RETENTION_MS =  24 * 60 * 60 * 1000L

        /** Open sessions older than 6 hours are marked STALE. */
        private const val STALE_SESSION_THRESHOLD_MS = 6 * 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val now = System.currentTimeMillis()

            // Delete synced logs older than 1 day
            val logCutoff = now - LOG_RETENTION_MS
            db.trackerLogDao().deleteOldSyncedLogs(logCutoff)

            // Delete synced metrics older than 7 days
            val metricsCutoff = now - METRICS_RETENTION_MS
            db.appMetricsDao().deleteOldSyncedMetrics(metricsCutoff)

            // Mark stale open sessions (open > 6 hours)
            val staleCutoff = now - STALE_SESSION_THRESHOLD_MS
            db.activitySessionDao().markStaleSessions(staleCutoff, now)

            Log.d(TAG, "Cleanup completed")
            TrackerLogger.i(TAG, "CLEANUP_COMPLETED",
                "Cleanup done — logs cutoff: ${logCutoff}, metrics cutoff: ${metricsCutoff}")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup failed", e)
            TrackerLogger.e(TAG, "CLEANUP_COMPLETED", "Cleanup failed: ${e.message}")
            Result.retry()
        }
    }
}
