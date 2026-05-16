package com.karthik.lifeos.tracker.sync.dto

import java.time.Instant

/**
 * DTO matching the backend's TrackerLogDto.
 */
data class TrackerLogDto(
    val timestamp: String,     // ISO-8601 Instant string
    val level: String,
    val tag: String,
    val eventType: String?,
    val message: String
) {
    companion object {
        fun fromEpochMillis(millis: Long): String {
            return Instant.ofEpochMilli(millis).toString()
        }
    }
}
