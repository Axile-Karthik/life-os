package com.karthik.lifeos.tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Structured internal tracker log entry.
 *
 * This is NOT raw Logcat output — it's a curated, structured logging system
 * for observability. Each entry has a typed eventType for Grafana filtering.
 *
 * Lifecycle: stored → synced to backend → marked synced → cleaned up by CleanupWorker.
 */
@Entity(tableName = "tracker_logs")
data class TrackerLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long,

    /** Log level: INFO, WARN, ERROR */
    val level: String,

    /** Source component: SessionManager, SyncManager, TrackerService, etc. */
    val tag: String,

    /**
     * Structured event type for Grafana queries.
     * Values: SESSION_CREATED, SESSION_SKIPPED, SESSION_HEALED, SESSION_IMPLICIT_END,
     * SCAN_STARTED, SCAN_COMPLETED, SYNC_STARTED, SYNC_SUCCESS, SYNC_FAILED,
     * METRICS_CAPTURED, CLEANUP_COMPLETED
     */
    val eventType: String,

    val message: String,

    /** Whether this log has been synced to the backend. */
    val isSynced: Boolean = false
)
