package com.karthik.lifeos.tracker.network

import com.karthik.lifeos.tracker.sync.SyncPayload
import com.karthik.lifeos.tracker.sync.SyncResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for the Life OS backend API.
 *
 * The backend is self-hosted and reachable via Tailscale VPN.
 * Base URL is configured via BuildConfig.BACKEND_URL.
 */
interface LifeOsApi {

    /**
     * POST /api/sync
     *
     * Sends a batch of activity sessions, tracker logs, and metrics snapshots.
     * The backend deduplicates sessions using (deviceId, packageName, startTime).
     */
    @POST("api/sync")
    suspend fun sync(@Body payload: SyncPayload): SyncResponse
}
