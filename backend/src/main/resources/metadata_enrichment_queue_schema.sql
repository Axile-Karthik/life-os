CREATE TABLE metadata_enrichment_queue (
    id UUID PRIMARY KEY,
    raw_title VARCHAR(500),
    normalized_title VARCHAR(500),
    content_type VARCHAR(50),
    source_platform VARCHAR(100),
    external_source VARCHAR(100),
    queue_status VARCHAR(50),
    retry_count INTEGER DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    processed_at TIMESTAMP
);
