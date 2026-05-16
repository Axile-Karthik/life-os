package com.karthik.lifeos.tracker.metrics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.karthik.lifeos.tracker.data.local.AppMetrics
import com.karthik.lifeos.tracker.data.local.AppMetricsDao
import com.karthik.lifeos.tracker.data.local.ActivitySessionDao
import com.karthik.lifeos.tracker.data.local.TrackerLogDao
import com.karthik.lifeos.tracker.logging.TrackerLogger
import com.karthik.lifeos.tracker.tracker.TrackerService

/**
 * Collects device telemetry and tracker health metrics.
 *
 * Captures: JVM heap, battery, WiFi, pending queue sizes, scan duration.
 * Stores snapshots in Room for sync to backend → Grafana time-series.
 *
 * Call [capture] after scans, before/after sync, and after failures.
 */
class MetricsCollector(
    private val context: Context,
    private val metricsDao: AppMetricsDao,
    private val sessionDao: ActivitySessionDao,
    private val logDao: TrackerLogDao
) {

    companion object {
        private const val TAG = "MetricsCollector"
    }

    /**
     * Capture a metrics snapshot and persist to Room.
     *
     * @param lastScanDurationMs Duration of the most recent scan in millis
     * @param sessionsFoundInScan Number of sessions found in the most recent scan
     */
    suspend fun capture(lastScanDurationMs: Long = 0, sessionsFoundInScan: Int = 0) {
        try {
            val runtime = Runtime.getRuntime()
            val metrics = AppMetrics(
                timestamp = System.currentTimeMillis(),
                heapUsedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                heapTotalMb = runtime.totalMemory() / (1024 * 1024),
                heapMaxMb = runtime.maxMemory() / (1024 * 1024),
                lastScanDurationMs = lastScanDurationMs,
                sessionsFoundInScan = sessionsFoundInScan,
                pendingSessionsCount = sessionDao.getUnsyncedCount(),
                pendingLogsCount = logDao.getUnsyncedCount(),
                pendingMetricsCount = metricsDao.getUnsyncedCount(),
                batteryLevel = getBatteryLevel(),
                isCharging = isCharging(),
                isWifiConnected = isWifiConnected(),
                trackerServiceRunning = TrackerService.isRunning
            )
            metricsDao.insert(metrics)
            TrackerLogger.i(TAG, "METRICS_CAPTURED",
                "heap=${metrics.heapUsedMb}MB, battery=${metrics.batteryLevel}%, " +
                "pending=${metrics.pendingSessionsCount}s/${metrics.pendingLogsCount}l")
        } catch (e: Exception) {
            TrackerLogger.e(TAG, "METRICS_CAPTURED", "Failed to capture metrics: ${e.message}")
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        return if (scale > 0) (level * 100) / scale else -1
    }

    private fun isCharging(): Boolean {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
