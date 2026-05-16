package com.karthik.lifeos.tracker.sync.dto

import java.time.Instant

/**
 * DTO matching the backend's AppMetricsDto.
 */
data class AppMetricsDto(
    val timestamp: String,     // ISO-8601 Instant string
    val heapUsedMb: Double,
    val heapTotalMb: Double,
    val heapMaxMb: Double,
    val lastScanDurationMs: Long,
    val sessionsFoundInScan: Int,
    val pendingSessionsCount: Int,
    val pendingLogsCount: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isWifiConnected: Boolean
) {
    companion object {
        fun fromEpochMillis(millis: Long): String {
            return Instant.ofEpochMilli(millis).toString()
        }
    }
}
