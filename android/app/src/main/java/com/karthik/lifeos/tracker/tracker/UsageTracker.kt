package com.karthik.lifeos.tracker.tracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Wraps UsageStatsManager to query foreground/background transition events.
 *
 * Why UsageStatsManager?
 * Android does not allow apps to observe other apps' lifecycles directly.
 * UsageStatsManager is the ONLY sanctioned API for querying app usage history.
 * It requires the user to manually grant "Usage Access" in Settings.
 *
 * The data is NOT real-time — events appear with a slight delay.
 * This is acceptable because we reconstruct sessions retroactively.
 */
class UsageTracker(context: Context) {

    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    /**
     * Query all foreground/background transition events in the given time range.
     *
     * On API 29+, MOVE_TO_FOREGROUND/MOVE_TO_BACKGROUND are deprecated.
     * We use ACTIVITY_RESUMED/ACTIVITY_PAUSED instead.
     * On API 26-28, we use the original event types.
     */
    companion object {
        private const val TAG = "UsageTracker"
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        private fun fmtTime(millis: Long): String = timeFormat.format(Date(millis))
    }

    fun queryEvents(startTime: Long, endTime: Long): List<AppEvent> {
        Log.d(TAG, "═══ SCANNING window: ${fmtTime(startTime)} → ${fmtTime(endTime)} ═══")

        val events = mutableListOf<AppEvent>()
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime) ?: run {
            Log.w(TAG, "queryEvents returned null!")
            return events
        }
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            val type = classifyEvent(event.eventType) ?: continue

            Log.d(TAG, "  📱 ${fmtTime(event.timeStamp)} | ${type.name.padEnd(10)} | ${event.packageName}")

            events.add(
                AppEvent(
                    packageName = event.packageName,
                    eventType = type,
                    timestamp = event.timeStamp
                )
            )
        }

        Log.d(TAG, "═══ Total events: ${events.size} ═══")
        return events
    }

    /**
     * Maps raw event type integers to our simplified EventType enum.
     * Returns null for event types we don't care about.
     */
    private fun classifyEvent(rawType: Int): EventType? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+: use the non-deprecated event types
            when (rawType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> EventType.FOREGROUND
                UsageEvents.Event.ACTIVITY_PAUSED -> EventType.BACKGROUND
                else -> null
            }
        } else {
            // API 26-28: use the original event types
            @Suppress("DEPRECATION")
            when (rawType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> EventType.FOREGROUND
                UsageEvents.Event.MOVE_TO_BACKGROUND -> EventType.BACKGROUND
                else -> null
            }
        }
    }
}

/** Simplified event type — we only care about foreground/background transitions. */
enum class EventType {
    FOREGROUND,
    BACKGROUND
}

/** A single app transition event extracted from UsageStatsManager. */
data class AppEvent(
    val packageName: String,
    val eventType: EventType,
    val timestamp: Long
)
