package com.karthik.lifeos.tracker.sync

/**
 * Response from POST /api/sync.
 * Matches the backend's SyncResponse DTO structure.
 */
data class SyncResponse(
    val success: Boolean,
    val syncedSessions: Int,
    val syncedLogs: Int,
    val message: String?
)
