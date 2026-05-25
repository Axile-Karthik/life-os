CREATE TABLE activity_session (
    id UUID PRIMARY KEY,
    metadata_id UUID,
    package_name VARCHAR(500),
    raw_title VARCHAR(500),
    normalized_title VARCHAR(500),
    content_type VARCHAR(50),
    source_platform VARCHAR(100),
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    duration_seconds BIGINT,
    session_state VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_session_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES content_metadata(id)
);
