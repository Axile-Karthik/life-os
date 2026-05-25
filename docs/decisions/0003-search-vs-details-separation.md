# ADR 0003: Search vs. Details Architecture Separation

## Status
Accepted

## Context
The Life OS metadata platform must support fast discovery across multiple external providers. This ADR formalizes the separation between lightweight discovery (Search) and rich persistence (Details) to ensure performance and prevent provider-leakage into the frontend.

## Decision
We will maintain a strict separation between discovery and enrichment layers.

### 1. Search (Discovery) — Lightweight & Fast
*   **Purpose**: Ephemeral discovery for search cards, wishlist flows, and quick-add interactions.
*   **Canonical DTO Structure**:
    ```json
    {
      "id": "IGDB_1331",
      "title": "Limbo",
      "imageUrl": "https://...",
      "releaseDate": "2010-07-21",
      "contentType": "GAME",
      "externalSource": "IGDB",
      "externalId": "1331"
    }
    ```
*   **Constraints**:
    *   Optimize for **fast discovery**, NOT completeness.
    *   **Allowed fields**: title, imageUrl, releaseDate, contentType, provider identity.
    *   **Prohibited fields**: screenshots, trailers, genres, storyline, developers, publishers, seasons, episodes, etc.
*   **Backend Role**: Acts as a **Normalization Layer**, never a passthrough proxy. Frontend must never depend on raw provider schemas.

### 2. Details (Enrichment) — Rich & Intentional
*   **Purpose**: Intentional enrichment and long-term persistence.
*   **Allowed Fields**: Full metadata including heavy assets (storyline, genres, platforms, ratings, trailers, etc.).
*   **Trigger**: Intentional user action (click-to-add) or background enrichment job.

### 3. Identity Rules
*   **Discovery Identity**: Uses the format `SOURCE_ID` (e.g., `IGDB_1331`) as a temporary ephemeral ID.
*   **Persistent Identity**: Future evolution will map these to internal UUIDs and canonical identity layers, but for Phase 1, the `id` vs `externalId` distinction is mandatory.

### 4. Future Compatibility
The architecture remains open to:
*   `matchScore`: For telemetry correlation and fuzzy matching.
*   `metadataState`: To distinguish between `DISCOVERED` and `TRACKED` content.

## Consequences
*   **UX**: Extremely fast debounced search workflows.
*   **Scalability**: Adding new providers (TMDB, Spotify, etc.) only requires implementing a lightweight mapper.
*   **Maintainability**: Provider-specific schema changes are isolated to the backend provider layer.
