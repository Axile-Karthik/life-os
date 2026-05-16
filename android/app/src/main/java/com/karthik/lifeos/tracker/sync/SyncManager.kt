package com.karthik.lifeos.tracker.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.karthik.lifeos.tracker.data.local.ActivitySessionDao
import com.karthik.lifeos.tracker.data.local.AppMetricsDao
import com.karthik.lifeos.tracker.data.local.TrackerLogDao
import com.karthik.lifeos.tracker.logging.TrackerLogger
import com.karthik.lifeos.tracker.network.RetrofitClient
import com.karthik.lifeos.tracker.sync.dto.AppMetricsDto
import com.karthik.lifeos.tracker.sync.dto.SessionDto
import com.karthik.lifeos.tracker.sync.dto.TrackerLogDto

/**
 * Orchestrates syncing pending data from Room to the Life OS backend.
 *
 * Design principles:
 * - Room is the durable retry queue — no complex retry logic needed
 * - Sync is idempotent — backend deduplicates via (deviceId, packageName, startTime)
 * - Offline-first — sync silently skips when network unavailable
 * - Only syncs completed sessions (endTime > 0) — open sessions are still evolving
 */
class SyncManager(
    private val context: Context,
    private val sessionDao: ActivitySessionDao,
    private val logDao: TrackerLogDao,
    private val metricsDao: AppMetricsDao
) {

    companion object {
        private const val TAG = "SyncManager"
    }

    /**
     * Attempt to sync all pending data to the backend.
     *
     * @return true if sync succeeded or nothing to sync, false on failure
     */
    suspend fun sync(): Boolean {
        // 1. Check network availability
        if (!isNetworkAvailable()) {
            TrackerLogger.i(TAG, "SYNC_STARTED", "Sync skipped — no network")
            return false
        }

        // 2. Gather pending data
        val sessions = sessionDao.getUnsyncedSessions()
        val logs = logDao.getUnsyncedLogs()
        val metrics = metricsDao.getUnsyncedMetrics()

        if (sessions.isEmpty() && logs.isEmpty() && metrics.isEmpty()) {
            return true // Nothing to sync
        }

        TrackerLogger.i(TAG, "SYNC_STARTED",
            "Syncing: ${sessions.size} sessions, ${logs.size} logs, ${metrics.size} metrics")

        return try {
            // 3. Build payload (map Room entities to backend DTOs)
            val payload = buildPayload(sessions, logs, metrics)

            // 4. POST to backend
            val response = RetrofitClient.api.sync(payload)

            // 5. Mark rows as synced on success
            if (response.success) {
                if (sessions.isNotEmpty()) {
                    sessionDao.markSessionsSynced(sessions.map { it.id })
                }
                if (logs.isNotEmpty()) {
                    logDao.markLogsSynced(logs.map { it.id })
                }
                if (metrics.isNotEmpty()) {
                    metricsDao.markMetricsSynced(metrics.map { it.id })
                }

                TrackerLogger.i(TAG, "SYNC_SUCCESS",
                    "Synced: ${response.syncedSessions} sessions, ${response.syncedLogs} logs")
                true
            } else {
                TrackerLogger.w(TAG, "SYNC_FAILED",
                    "Backend returned failure: ${response.message}")
                false
            }
        } catch (e: Exception) {
            TrackerLogger.e(TAG, "SYNC_FAILED",
                "Sync exception: ${e.message}")
            false
        }
    }

    private fun buildPayload(
        sessions: List<com.karthik.lifeos.tracker.data.local.ActivitySession>,
        logs: List<com.karthik.lifeos.tracker.data.local.TrackerLog>,
        metrics: List<com.karthik.lifeos.tracker.data.local.AppMetrics>
    ): SyncPayload {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown-device"

        val sessionDtos = sessions.map { session ->
            SessionDto(
                source = "android",
                type = "game",
                packageName = session.packageName,
                title = session.gameName,
                startTime = SessionDto.fromEpochMillis(session.startTime),
                endTime = SessionDto.fromEpochMillis(session.endTime),
                durationMillis = session.durationMillis
            )
        }

        val logDtos = logs.map { log ->
            TrackerLogDto(
                timestamp = TrackerLogDto.fromEpochMillis(log.timestamp),
                level = log.level,
                tag = log.tag,
                eventType = log.eventType,
                message = log.message
            )
        }

        val metricDtos = metrics.map { m ->
            AppMetricsDto(
                timestamp = AppMetricsDto.fromEpochMillis(m.timestamp),
                heapUsedMb = m.heapUsedMb.toDouble(),
                heapTotalMb = m.heapTotalMb.toDouble(),
                heapMaxMb = m.heapMaxMb.toDouble(),
                lastScanDurationMs = m.lastScanDurationMs,
                sessionsFoundInScan = m.sessionsFoundInScan,
                pendingSessionsCount = m.pendingSessionsCount,
                pendingLogsCount = m.pendingLogsCount,
                batteryLevel = m.batteryLevel,
                isCharging = m.isCharging,
                isWifiConnected = m.isWifiConnected
            )
        }

        return SyncPayload(
            deviceId = deviceId,
            sessions = sessionDtos,
            logs = logDtos,
            metrics = metricDtos
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
