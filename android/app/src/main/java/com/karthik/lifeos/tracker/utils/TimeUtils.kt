package com.karthik.lifeos.tracker.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    private val timestampFormat = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault())

    /** Format milliseconds duration as "1h 23m 45s" or "23m 45s" or "45s". */
    fun formatDuration(millis: Long): String {
        if (millis <= 0L) return "In progress"

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return buildString {
            if (hours > 0) append("${hours}h ")
            if (hours > 0 || minutes > 0) append("${minutes}m ")
            append("${seconds}s")
        }
    }

    /** Format epoch millis to a human-readable timestamp like "May 9, 2026  9:30 PM". */
    fun formatTimestamp(millis: Long): String {
        if (millis <= 0L) return "—"
        return timestampFormat.format(Date(millis))
    }
}
