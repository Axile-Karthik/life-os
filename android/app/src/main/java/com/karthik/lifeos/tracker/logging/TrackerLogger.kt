package com.karthik.lifeos.tracker.logging

import android.util.Log
import com.karthik.lifeos.tracker.data.local.TrackerLog
import com.karthik.lifeos.tracker.data.local.TrackerLogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Centralized structured logging system for the tracker.
 *
 * All log entries are:
 * 1. Forwarded to Android Logcat (for development)
 * 2. Persisted to Room (for sync to backend + Grafana)
 *
 * Uses a dedicated coroutine scope with SupervisorJob so that
 * individual write failures don't crash the logging pipeline.
 *
 * Must be initialized via [init] before first use (called from GameTimeApp.onCreate).
 */
object TrackerLogger {

    private var dao: TrackerLogDao? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Initialize with the TrackerLogDao from AppDatabase.
     * Call once from Application.onCreate().
     */
    fun init(dao: TrackerLogDao) {
        this.dao = dao
    }

    /** Log at INFO level. */
    fun i(tag: String, eventType: String, message: String) {
        log("INFO", tag, eventType, message)
    }

    /** Log at WARN level. */
    fun w(tag: String, eventType: String, message: String) {
        log("WARN", tag, eventType, message)
    }

    /** Log at ERROR level. */
    fun e(tag: String, eventType: String, message: String) {
        log("ERROR", tag, eventType, message)
    }

    private fun log(level: String, tag: String, eventType: String, message: String) {
        // Always forward to Logcat for development visibility
        val logcatMsg = "[$eventType] $message"
        when (level) {
            "ERROR" -> Log.e(tag, logcatMsg)
            "WARN" -> Log.w(tag, logcatMsg)
            else -> Log.d(tag, logcatMsg)
        }

        // Persist to Room asynchronously
        val currentDao = dao ?: return
        scope.launch {
            try {
                currentDao.insert(
                    TrackerLog(
                        timestamp = System.currentTimeMillis(),
                        level = level,
                        tag = tag,
                        eventType = eventType,
                        message = message
                    )
                )
            } catch (e: Exception) {
                // Last resort — don't let logging crashes affect the tracker
                Log.e("TrackerLogger", "Failed to persist log entry", e)
            }
        }
    }
}
