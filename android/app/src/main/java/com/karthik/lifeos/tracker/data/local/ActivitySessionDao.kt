package com.karthik.lifeos.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActivitySessionDao {

    /**
     * Insert a session. If a session with the same (packageName, startTime) already exists,
     * it is silently ignored — this is critical for dedup across overlapping scan windows.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: ActivitySession)

    /**
     * Insert multiple sessions in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSessions(sessions: List<ActivitySession>)

    /**
     * Return all sessions ordered by most recent first.
     */
    @Query("SELECT * FROM activity_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<ActivitySession>

    /**
     * Return sessions that started after the given timestamp.
     * Useful for incremental UI refreshes.
     */
    @Query("SELECT * FROM activity_sessions WHERE startTime >= :timestamp ORDER BY startTime DESC")
    suspend fun getSessionsAfter(timestamp: Long): List<ActivitySession>

    // ── Open session support ─────────────────────────────────────────

    /**
     * Return all sessions that are still open (game was in foreground when the
     * scan window ended, so endTime was recorded as 0).
     */
    @Query("SELECT * FROM activity_sessions WHERE endTime = 0")
    suspend fun getOpenSessions(): List<ActivitySession>

    /**
     * Return the most recent open session for a given package.
     * Used by orphaned-PAUSED healing to find the session
     * that this PAUSED event likely belongs to.
     */
    @Query("SELECT * FROM activity_sessions WHERE packageName = :packageName AND endTime = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestOpenSessionForPackage(packageName: String): ActivitySession?

    /**
     * Close an open session by setting its endTime, computed duration, state, and end reason.
     */
    @Query("UPDATE activity_sessions SET endTime = :endTime, durationMillis = :durationMillis, sessionState = :state, sessionEndReason = :reason WHERE id = :id")
    suspend fun updateSessionEnd(id: Long, endTime: Long, durationMillis: Long, state: String = SessionState.CLOSED.name, reason: String = SessionEndReason.NORMAL_BACKGROUND.name)

    // ── Aggregated statistics ────────────────────────────────────────

    /**
     * Return per-game aggregated stats: total play time and session count.
     * Only counts completed sessions (endTime > 0) to avoid skewing stats
     * with in-progress sessions that have durationMillis = 0.
     */
    @Query("""
        SELECT packageName,
               MAX(gameName) AS gameName,
               SUM(durationMillis) AS totalPlayTimeMillis,
               COUNT(*) AS sessionCount
        FROM activity_sessions
        WHERE endTime > 0
        GROUP BY packageName
        ORDER BY totalPlayTimeMillis DESC
    """)
    suspend fun getActivityStats(): List<ActivityStats>

    // ── Sync support ─────────────────────────────────────────────────

    /**
     * Return completed sessions that haven't been synced to the backend yet.
     * Only syncs completed sessions (endTime > 0) — open sessions are still evolving.
     */
    @Query("SELECT * FROM activity_sessions WHERE isSynced = 0 AND endTime > 0 ORDER BY startTime ASC")
    suspend fun getUnsyncedSessions(): List<ActivitySession>

    /**
     * Mark sessions as synced after successful backend POST.
     */
    @Query("UPDATE activity_sessions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSessionsSynced(ids: List<Long>)

    /** Count of unsynced completed sessions — used by MetricsCollector. */
    @Query("SELECT COUNT(*) FROM activity_sessions WHERE isSynced = 0 AND endTime > 0")
    suspend fun getUnsyncedCount(): Int

    // ── Stale session management ─────────────────────────────────────

    /**
     * Mark open sessions older than cutoff as STALE.
     * These are sessions where the game never emitted a BACKGROUND event.
     */
    @Query("UPDATE activity_sessions SET sessionState = 'STALE', sessionEndReason = 'STALE_TIMEOUT', endTime = :now, durationMillis = :now - startTime WHERE endTime = 0 AND startTime < :cutoffTime")
    suspend fun markStaleSessions(cutoffTime: Long, now: Long = System.currentTimeMillis())

    /** Delete a single session by ID. */
    @Query("DELETE FROM activity_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    /** Delete ALL sessions — used for fresh start / testing. */
    @Query("DELETE FROM activity_sessions")
    suspend fun deleteAllSessions()
}
