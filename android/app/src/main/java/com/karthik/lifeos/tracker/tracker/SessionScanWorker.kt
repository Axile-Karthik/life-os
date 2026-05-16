package com.karthik.lifeos.tracker.tracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karthik.lifeos.tracker.data.local.AppDatabase
import com.karthik.lifeos.tracker.data.local.SessionEndReason
import com.karthik.lifeos.tracker.data.local.SessionState
import com.karthik.lifeos.tracker.logging.TrackerLogger
import com.karthik.lifeos.tracker.metrics.MetricsCollector
import com.karthik.lifeos.tracker.sync.SyncManager

/**
 * WorkManager worker that periodically scans for new game sessions.
 *
 * Scheduled to run every 15 minutes (WorkManager's minimum interval).
 * Queries a 20-minute window to ensure overlap and avoid gaps between scans.
 *
 * Execution flow:
 * 1. Query raw usage events for the current scan window.
 * 2. (Change 2) Heal previously-open sessions: query Room for sessions with
 *    endTime=0, check if a PAUSED event now exists in the current window,
 *    and close them.
 * 3. Run SessionManager.scanForSessions() which:
 *    - Reconstructs completed sessions
 *    - Emits new open sessions (endTime=0)
 *    - Heals orphaned PAUSEs (Change 3)
 * 4. Insert all new sessions (duplicates silently ignored via IGNORE strategy).
 * 5. Capture metrics and trigger sync.
 */
class SessionScanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "game_session_scan"
        private const val TAG = "SessionScanWorker"

        /** Query window: 20 minutes to overlap with the 15-minute schedule interval. */
        private const val SCAN_WINDOW_MS = 20 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        return try {
            val scanStart = System.currentTimeMillis()
            TrackerLogger.i(TAG, "SCAN_STARTED", "Worker scan starting")

            val endTime = System.currentTimeMillis()
            val startTime = endTime - SCAN_WINDOW_MS

            val db = AppDatabase.getInstance(applicationContext)
            val dao = db.activitySessionDao()

            // ── Change 2: Heal open sessions from previous scans ─────────
            // Before reconstructing new sessions, check if any previously-open
            // sessions now have a matching PAUSED event in the current window.
            val usageTracker = UsageTracker(applicationContext)
            val currentEvents = usageTracker.queryEvents(startTime, endTime)
            val openSessions = dao.getOpenSessions()

            var healedFromOpen = 0
            for (session in openSessions) {
                // Find the first BACKGROUND event for this package that occurred
                // after the session's startTime. This is the "closing" event.
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
                    healedFromOpen++
                    Log.d(TAG, "Healed open session (BACKGROUND) for ${session.packageName}: duration=${duration/1000}s")
                    TrackerLogger.i(TAG, "SESSION_HEALED",
                        "${session.gameName} healed via BACKGROUND — ${duration/1000}s")
                } else {
                    // SMART HEALING: No BACKGROUND event for this game,
                    // but maybe the user switched to ANOTHER app.
                    val switchEvent = currentEvents
                        .filter { it.timestamp > session.startTime && it.packageName != session.packageName }
                        .minByOrNull { it.timestamp }

                    if (switchEvent != null) {
                        val duration = switchEvent.timestamp - session.startTime
                        if (duration >= 5000) { // 5s minimum
                            dao.updateSessionEnd(
                                session.id, switchEvent.timestamp, duration,
                                SessionState.IMPLICIT_END.name, SessionEndReason.APP_SWITCH.name
                            )
                            healedFromOpen++
                            Log.d(TAG, "Healed open session (IMPLICIT) for ${session.packageName} via switch to ${switchEvent.packageName}: duration=${duration/1000}s")
                            TrackerLogger.i(TAG, "SESSION_IMPLICIT_END",
                                "${session.gameName} implicit end via app switch — ${duration/1000}s")
                        }
                    }
                }
            }

            // ── Normal reconstruction (also handles Change 2 new opens + Change 3 orphans) ──
            val sessionManager = SessionManager(applicationContext, dao)
            val scanResult = sessionManager.scanForSessions(startTime, endTime)

            if (scanResult.sessions.isNotEmpty()) {
                dao.insertSessions(scanResult.sessions) // Duplicates silently ignored
            }

            val scanDuration = System.currentTimeMillis() - scanStart
            Log.d(TAG, "Scan complete: ${scanResult.sessions.size} session(s), " +
                    "$healedFromOpen healed from open, ${scanResult.healedCount} healed from orphans")
            TrackerLogger.i(TAG, "SCAN_COMPLETED",
                "Worker scan done — ${scanResult.sessions.size} sessions, " +
                "$healedFromOpen healed, ${scanResult.healedCount} orphans, ${scanDuration}ms")

            // Capture metrics after scan
            val metricsCollector = MetricsCollector(
                applicationContext, db.appMetricsDao(), dao, db.trackerLogDao()
            )
            metricsCollector.capture(
                lastScanDurationMs = scanDuration,
                sessionsFoundInScan = scanResult.sessions.size
            )

            // Trigger sync
            try {
                val syncManager = SyncManager(
                    applicationContext, dao, db.trackerLogDao(), db.appMetricsDao()
                )
                syncManager.sync()
            } catch (e: Exception) {
                TrackerLogger.e(TAG, "SYNC_FAILED", "Post-scan sync failed: ${e.message}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Session scan failed", e)
            TrackerLogger.e(TAG, "SCAN_COMPLETED", "Worker scan failed: ${e.message}")
            Result.retry()
        }
    }
}
