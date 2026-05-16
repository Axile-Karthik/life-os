package com.karthik.lifeos.tracker.tracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.karthik.lifeos.tracker.data.local.AppDatabase
import com.karthik.lifeos.tracker.data.local.SessionEndReason
import com.karthik.lifeos.tracker.data.local.SessionState
import com.karthik.lifeos.tracker.logging.TrackerLogger
import com.karthik.lifeos.tracker.metrics.MetricsCollector
import com.karthik.lifeos.tracker.sync.SyncManager
import com.karthik.lifeos.tracker.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service for near-real-time game session tracking.
 *
 * Scans UsageStatsManager every 90 seconds while active. This complements
 * the WorkManager fallback (which runs every 15 minutes) by providing
 * much faster session detection when the user has tracking enabled.
 *
 * Why a foreground service?
 * - WorkManager's minimum interval is 15 minutes — too slow for live tracking
 * - A foreground service is allowed to run continuously with a visible notification
 * - START_STICKY ensures the system restarts it if killed under memory pressure
 */
class TrackerService : Service() {

    companion object {
        private const val TAG = "TrackerService"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "game_tracker_channel"

        /** Scan every 90 seconds — balanced between responsiveness and battery. */
        private const val SCAN_INTERVAL_MS = 90_000L

        /** Query window: 5 minutes to ensure overlap between 90-second scans. */
        private const val SCAN_WINDOW_MS = 5 * 60 * 1000L

        @Volatile
        var isRunning = false
            private set

        fun start(context: Context) {
            context.startForegroundService(Intent(context, TrackerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackerService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startPeriodicScan()
        Log.d(TAG, "Tracker service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        scanJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "Tracker service stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startPeriodicScan() {
        scanJob = serviceScope.launch {
            while (isActive) {
                try {
                    performScan()
                } catch (e: Exception) {
                    Log.e(TAG, "Scan failed", e)
                    TrackerLogger.e(TAG, "SCAN_COMPLETED", "Scan failed: ${e.message}")
                }
                delay(SCAN_INTERVAL_MS)
            }
        }
    }

    /**
     * Same logic as SessionScanWorker but runs inline in the service coroutine.
     * Heals open sessions, reconstructs new ones, persists to Room,
     * then captures metrics and triggers sync.
     */
    private suspend fun performScan() {
        val scanStart = System.currentTimeMillis()
        TrackerLogger.i(TAG, "SCAN_STARTED", "Service scan starting")

        val endTime = System.currentTimeMillis()
        val startTime = endTime - SCAN_WINDOW_MS
        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.activitySessionDao()

        // Heal previously-open sessions
        val usageTracker = UsageTracker(applicationContext)
        val currentEvents = usageTracker.queryEvents(startTime, endTime)
        val openSessions = dao.getOpenSessions()

        for (session in openSessions) {
            val pauseEvent = currentEvents.firstOrNull { event ->
                event.packageName == session.packageName &&
                event.eventType == EventType.BACKGROUND &&
                event.timestamp > session.startTime
            }
            if (pauseEvent != null) {
                val duration = pauseEvent.timestamp - session.startTime
                dao.updateSessionEnd(
                    session.id, pauseEvent.timestamp, duration,
                    SessionState.HEALED.name, SessionEndReason.NORMAL_BACKGROUND.name
                )
            }
        }

        // Reconstruct new sessions
        val sessionManager = SessionManager(applicationContext, dao)
        val result = sessionManager.scanForSessions(startTime, endTime)
        if (result.sessions.isNotEmpty()) {
            dao.insertSessions(result.sessions)
            Log.d(TAG, "Found ${result.sessions.size} session(s)")
        }

        val scanDuration = System.currentTimeMillis() - scanStart
        TrackerLogger.i(TAG, "SCAN_COMPLETED",
            "Service scan done — ${result.sessions.size} sessions, ${result.healedCount} healed, ${scanDuration}ms")

        // Capture metrics after scan
        val metricsCollector = MetricsCollector(
            applicationContext, db.appMetricsDao(), dao, db.trackerLogDao()
        )
        metricsCollector.capture(
            lastScanDurationMs = scanDuration,
            sessionsFoundInScan = result.sessions.size
        )

        // Trigger sync (fire-and-forget, errors are logged)
        try {
            val syncManager = SyncManager(
                applicationContext, dao, db.trackerLogDao(), db.appMetricsDao()
            )
            syncManager.sync()
        } catch (e: Exception) {
            TrackerLogger.e(TAG, "SYNC_FAILED", "Post-scan sync failed: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Game Tracker",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when game session tracking is active"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Life OS Tracker")
            .setContentText("Tracking activity sessions")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
