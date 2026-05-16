package com.karthik.lifeos.tracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single activity session — either completed or in-progress.
 *
 * The unique index on (packageName, startTime) prevents duplicate sessions
 * from being inserted when the WorkManager worker scans overlapping time windows.
 *
 * An in-progress (open) session has endTime = 0 and durationMillis = 0.
 * It gets "healed" when a matching PAUSED event is found in a later scan.
 *
 * Sessions are NEVER deleted locally — they remain permanently for
 * offline analytics and future timeline replay.
 */
@Entity(
    tableName = "activity_sessions",
    indices = [Index(value = ["packageName", "startTime"], unique = true)]
)
data class ActivitySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Stable UUID — survives retries, sync failures, and backend dedup. */
    val sessionId: String = UUID.randomUUID().toString(),

    val packageName: String,
    val gameName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,

    /** Lifecycle state — see [SessionState]. Stored as enum name string. */
    val sessionState: String = SessionState.OPEN.name,

    /** How the session ended — see [SessionEndReason]. Null for open sessions. */
    val sessionEndReason: String? = null,

    /** Whether this session has been synced to the backend. */
    val isSynced: Boolean = false
)

/** Lifecycle state of a session. */
enum class SessionState {
    OPEN,
    CLOSED,
    HEALED,
    IMPLICIT_END,
    PARTIAL,
    STALE
}

/** How a session was ended/closed. */
enum class SessionEndReason {
    NORMAL_BACKGROUND,
    APP_SWITCH,
    DOUBLE_FOREGROUND,
    ORPHAN_HEAL,
    STALE_TIMEOUT
}
