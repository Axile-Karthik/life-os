# Telemetry Ingestion Flow

1. **Android Tracker** captures device activity, foreground app changes, and usage stats.
2. The tracker POSTs batches of raw `TrackerLog` data to the `backend` telemetry module endpoint (`/api/v1/telemetry/logs`).
3. The `telemetry` module validates the payload, assigns a server-side timestamp, and persists the raw records into PostgreSQL.
4. Internal Spring Events (`TelemetryIngestedEvent`) are fired for asynchronous consumers (e.g., Timeline correlation, Metadata enrichment).
5. Observability metrics (Micrometer) are incremented for ingestion throughput tracking, which are then scraped by Prometheus.
