package com.karthik.lifeos.tracker.sync.dto

import java.time.Instant

/**
 * DTO matching the backend's SessionDto.
 * Maps Android ActivitySession fields to the backend's expected shape.
 */
data class SessionDto(
    val source: String,
    val type: String,
    val packageName: String,
    val title: String,
    val startTime: String,     // ISO-8601 Instant string
    val endTime: String,       // ISO-8601 Instant string
    val durationMillis: Long
) {
    companion object {
        /**
         * Convert epoch millis to ISO-8601 Instant string for backend compatibility.
         * Backend uses java.time.Instant which Jackson deserializes from ISO strings.
         */
        fun fromEpochMillis(millis: Long): String {
            return Instant.ofEpochMilli(millis).toString()
        }
    }
}
