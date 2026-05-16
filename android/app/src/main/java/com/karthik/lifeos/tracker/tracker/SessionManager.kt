package com.karthik.lifeos.tracker.tracker

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.karthik.lifeos.tracker.data.local.ActivitySession
import com.karthik.lifeos.tracker.data.local.ActivitySessionDao
import com.karthik.lifeos.tracker.data.local.SessionState
import com.karthik.lifeos.tracker.data.local.SessionEndReason
import com.karthik.lifeos.tracker.logging.TrackerLogger

/**
 * Reconstructs game sessions from raw UsageStatsManager events.
 *
 * Algorithm:
 * 1. Filter events to only game packages (via GameDetector).
 * 2. Group events by packageName.
 * 3. For each package, walk events chronologically:
 *    - FOREGROUND event → record as potential session start
 *    - BACKGROUND event → if a start exists, emit a completed session
 * 4. Discard completed sessions shorter than MIN_SESSION_DURATION_MS (noise filter).
 * 5. Emit open sessions (FOREGROUND with no BACKGROUND) with endTime=0 (Change 2).
 * 6. Collect orphaned BACKGROUND events and heal matching open sessions in Room (Change 3).
 *
 * Edge cases handled:
 * - Multiple FOREGROUND events without BACKGROUND → only the first start is kept
 * - BACKGROUND without preceding FOREGROUND → treated as orphaned, healed via Room
 * - Unpaired FOREGROUND at end of window → emitted as open session (endTime=0)
 */
class SessionManager(
    context: Context,
    private val dao: ActivitySessionDao
) {

    companion object {
        private const val TAG = "SessionManager"

        /** Ignore completed sessions shorter than 5 seconds — likely accidental opens. */
        private const val MIN_SESSION_DURATION_MS = 5_000L

        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        private fun fmtTime(millis: Long): String = if (millis > 0) timeFormat.format(Date(millis)) else "---"
    }

    private val usageTracker = UsageTracker(context)
    private val gameDetector = GameDetector(context)

    /**
     * Holds the output of a single scan pass.
     *
     * @param sessions  Both completed (endTime > 0) and open (endTime = 0) sessions
     * @param healedCount  Number of previously-open sessions closed via orphaned PAUSEs
     */
    data class ScanResult(
        val sessions: List<ActivitySession>,
        val healedCount: Int
    )

    /**
     * Scan the given time window, reconstruct sessions, and heal orphans.
     *
     * @param startTime Start of the scan window (epoch millis)
     * @param endTime   End of the scan window (epoch millis)
     * @return ScanResult containing new sessions and the count of healed open sessions
     */
    suspend fun scanForSessions(startTime: Long, endTime: Long): ScanResult {
        val allEvents = usageTracker.queryEvents(startTime, endTime)

        // Filter to game-only events
        val gameEvents = allEvents.filter { gameDetector.isGame(it.packageName) }
        Log.d(TAG, "Game events: ${gameEvents.size} out of ${allEvents.size} total events")

        // Reconstruct sessions per package
        val allSessions = mutableListOf<ActivitySession>()
        val allOrphanedPauses = mutableListOf<AppEvent>()

        gameEvents
            .groupBy { it.packageName }
            .forEach { (packageName, events) ->
                Log.d(TAG, "\n── Reconstructing: $packageName (${events.size} events) ──")
                val result = reconstructSessions(packageName, events.sortedBy { it.timestamp })
                allSessions.addAll(result.sessions)
                allOrphanedPauses.addAll(result.orphanedPauses)
            }

        // ── Change 3: Heal orphaned PAUSEs ──────────────────────────────
        // An orphaned PAUSE is a BACKGROUND event with no matching FOREGROUND
        // in this scan window. It likely belongs to an open session from a
        // previous scan. Query Room and close that session.
        var healedCount = 0
        for (pause in allOrphanedPauses) {
            val openSession = dao.getLatestOpenSessionForPackage(pause.packageName)
            if (openSession != null) {
                val duration = pause.timestamp - openSession.startTime
                // Guard: only heal if PAUSE is AFTER the session start and session is meaningful
                if (duration >= MIN_SESSION_DURATION_MS) {
                    dao.updateSessionEnd(
                        openSession.id, pause.timestamp, duration,
                        SessionState.HEALED.name, SessionEndReason.ORPHAN_HEAL.name
                    )
                    healedCount++
                    Log.d(TAG, "Healed orphaned PAUSE for ${pause.packageName}: duration=${duration/1000}s")
                    TrackerLogger.i(TAG, "SESSION_HEALED",
                        "${gameDetector.getAppName(pause.packageName)} healed via orphan PAUSE — ${duration/1000}s")
                } else {
                    Log.d(TAG, "Skipped orphan heal for ${pause.packageName}: duration=${duration}ms (invalid or too short)")
                }
            }
        }

        return ScanResult(sessions = allSessions, healedCount = healedCount)
    }

    // ── Internal reconstruction types ────────────────────────────────────

    /** Intermediate result from per-package reconstruction. */
    private data class ReconstructionResult(
        val sessions: List<ActivitySession>,
        val orphanedPauses: List<AppEvent>
    )

    /**
     * Given a sorted list of events for a single package, pair FOREGROUND→BACKGROUND
     * transitions into completed sessions. Unpaired FOREGROUNDs become open sessions;
     * unpaired BACKGROUNDs are returned as orphaned pauses.
     */
    private fun reconstructSessions(
        packageName: String,
        sortedEvents: List<AppEvent>
    ): ReconstructionResult {
        val sessions = mutableListOf<ActivitySession>()
        val orphanedPauses = mutableListOf<AppEvent>()
        var sessionStart: Long? = null

        for (event in sortedEvents) {
            when (event.eventType) {
                EventType.FOREGROUND -> {
                    if (sessionStart == null) {
                        sessionStart = event.timestamp
                        Log.d(TAG, "   🟢 FOREGROUND at ${fmtTime(event.timestamp)} → recording start")
                    } else {
                        // A second FOREGROUND without a BACKGROUND in between.
                        // This means the game was backgrounded and re-opened, but
                        // Android never reported the BACKGROUND event (common with
                        // some games). Implicitly end the previous session HERE.
                        val duration = event.timestamp - sessionStart
                        if (duration >= MIN_SESSION_DURATION_MS) {
                            Log.d(TAG, "   🟡 FOREGROUND at ${fmtTime(event.timestamp)} → IMPLICIT END of previous session: ${fmtTime(sessionStart)}→${fmtTime(event.timestamp)} (${duration/1000}s)")
                            val appName = gameDetector.getAppName(packageName)
                            sessions.add(
                                ActivitySession(
                                    packageName = packageName,
                                    gameName = appName,
                                    startTime = sessionStart,
                                    endTime = event.timestamp,
                                    durationMillis = duration,
                                    sessionState = SessionState.IMPLICIT_END.name,
                                    sessionEndReason = SessionEndReason.DOUBLE_FOREGROUND.name
                                )
                            )
                            TrackerLogger.i(TAG, "SESSION_IMPLICIT_END",
                                "$appName implicit end (double FOREGROUND) — ${duration/1000}s")
                        } else {
                            Log.d(TAG, "   🟡 FOREGROUND at ${fmtTime(event.timestamp)} → IMPLICIT END discarded (only ${duration/1000}s)")
                            TrackerLogger.i(TAG, "SESSION_SKIPPED",
                                "${gameDetector.getAppName(packageName)} implicit end too short — ${duration}ms")
                        }
                        sessionStart = event.timestamp // Start a new session
                        Log.d(TAG, "   🟢 FOREGROUND at ${fmtTime(event.timestamp)} → recording NEW start")
                    }
                }
                EventType.BACKGROUND -> {
                    val start = sessionStart
                    if (start == null) {
                        Log.d(TAG, "   🟠 BACKGROUND at ${fmtTime(event.timestamp)} → ORPHANED (no matching FOREGROUND)")
                        orphanedPauses.add(event)
                        continue
                    }

                    val duration = event.timestamp - start

                    if (duration >= MIN_SESSION_DURATION_MS) {
                        Log.d(TAG, "   🔴 BACKGROUND at ${fmtTime(event.timestamp)} → SESSION COMPLETE: ${fmtTime(start)}→${fmtTime(event.timestamp)} (${duration/1000}s)")
                        val appName = gameDetector.getAppName(packageName)
                        sessions.add(
                            ActivitySession(
                                packageName = packageName,
                                gameName = appName,
                                startTime = start,
                                endTime = event.timestamp,
                                durationMillis = duration,
                                sessionState = SessionState.CLOSED.name,
                                sessionEndReason = SessionEndReason.NORMAL_BACKGROUND.name
                            )
                        )
                        TrackerLogger.i(TAG, "SESSION_CREATED",
                            "$appName session — ${duration/1000}s")
                    } else {
                        Log.d(TAG, "   ⚪ BACKGROUND at ${fmtTime(event.timestamp)} → DISCARDED (only ${duration/1000}s < 5s minimum)")
                        TrackerLogger.i(TAG, "SESSION_SKIPPED",
                            "${gameDetector.getAppName(packageName)} too short — ${duration}ms")
                    }

                    sessionStart = null
                }
            }
        }

        // Open session handling
        if (sessionStart != null) {
            val elapsed = System.currentTimeMillis() - sessionStart
            if (elapsed >= MIN_SESSION_DURATION_MS) {
                Log.d(TAG, "   ⏳ OPEN SESSION: started at ${fmtTime(sessionStart)}, open for ${elapsed/1000}s → saving as In Progress")
                sessions.add(
                    ActivitySession(
                        packageName = packageName,
                        gameName = gameDetector.getAppName(packageName),
                        startTime = sessionStart,
                        endTime = 0L,
                        durationMillis = 0L,
                        sessionState = SessionState.OPEN.name,
                        sessionEndReason = null
                    )
                )
            } else {
                Log.d(TAG, "   ⏳ OPEN SESSION: started at ${fmtTime(sessionStart)}, only ${elapsed/1000}s → SKIPPED (too short)")
            }
        }

        Log.d(TAG, "   Result: ${sessions.size} session(s), ${orphanedPauses.size} orphan(s)")
        return ReconstructionResult(sessions, orphanedPauses)
    }
}
