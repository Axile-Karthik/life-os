package com.karthik.lifeos.tracker.data.local

/**
 * Aggregated activity statistics — not a Room entity, just a query result POJO.
 * Room maps the column aliases from the GROUP BY query directly to these fields.
 */
data class ActivityStats(
    val packageName: String,
    val gameName: String,
    val totalPlayTimeMillis: Long,
    val sessionCount: Int
)
