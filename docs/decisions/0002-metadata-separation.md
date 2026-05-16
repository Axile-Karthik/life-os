# Architecture Decision: Metadata Intelligence Separation

## Context
The platform ingests massive amounts of telemetry from various sources (Android, browsers, etc.). However, raw telemetry is often difficult to query or make sense of without context. Context is provided by external metadata (e.g., matching a YouTube video ID to its title, tags, and category, or an app package name to a specific tool).

## Decision
We are isolating Metadata Intelligence as a distinct bounded context within the ecosystem. The telemetry pipelines will be responsible for fast ingestion and storage, while the metadata module will asynchronously enrich this data.

## Consequences
- **Positive:** Telemetry ingestion remains lightweight and fast. The metadata system can scale its rate-limited external API calls independently.
- **Negative:** Requires an event-driven or scheduled enrichment pipeline to bridge the gap between raw telemetry and metadata.
