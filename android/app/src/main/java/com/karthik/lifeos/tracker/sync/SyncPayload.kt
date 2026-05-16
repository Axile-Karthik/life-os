package com.karthik.lifeos.tracker.sync

import com.karthik.lifeos.tracker.sync.dto.AppMetricsDto
import com.karthik.lifeos.tracker.sync.dto.SessionDto
import com.karthik.lifeos.tracker.sync.dto.TrackerLogDto

/**
 * Payload sent to POST /api/sync.
 * Matches the backend's SyncRequest DTO structure.
 */
data class SyncPayload(
    val deviceId: String,
    val sessions: List<SessionDto>,
    val logs: List<TrackerLogDto>,
    val metrics: List<AppMetricsDto>
)
