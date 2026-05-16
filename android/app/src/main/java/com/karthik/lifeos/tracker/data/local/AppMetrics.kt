package com.karthik.lifeos.tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Snapshot of tracker telemetry at a point in time.
 *
 * Captured after each scan, before/after sync, and after failures.
 * Provides Grafana with time-series data for: heap trends, battery, scan duration,
 * pending queue sizes, and tracker health.
 *
 * Lifecycle: stored → synced to backend → marked synced → cleaned up by CleanupWorker.
 */
@Entity(tableName = "app_metrics")
data class AppMetrics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long,

    // ── JVM Heap ──
    val heapUsedMb: Long,
    val heapTotalMb: Long,
    val heapMaxMb: Long,

    // ── Scan telemetry ──
    val lastScanDurationMs: Long,
    val sessionsFoundInScan: Int,

    // ── Pending queue sizes ──
    val pendingSessionsCount: Int,
    val pendingLogsCount: Int,
    val pendingMetricsCount: Int,

    // ── Device state ──
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isWifiConnected: Boolean,

    // ── Tracker state ──
    val trackerServiceRunning: Boolean,

    /** Whether this snapshot has been synced to the backend. */
    val isSynced: Boolean = false
)
