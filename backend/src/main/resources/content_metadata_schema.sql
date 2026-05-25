-- Schema definition for content_metadata table.
-- Serves as the canonical metadata identity layer for the entire Life OS ecosystem.

CREATE TABLE content_metadata (
    id UUID PRIMARY KEY,

    content_type VARCHAR(50) NOT NULL,

    title VARCHAR(500) NOT NULL,

    normalized_title VARCHAR(500) NOT NULL,

    description TEXT,

    image_url TEXT,

    backdrop_url TEXT,

    release_date DATE,

    genres JSONB,

    metadata_state VARCHAR(50) NOT NULL,

    external_source VARCHAR(100),

    external_id VARCHAR(255),

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Prevent duplicates based on normalized title per content type
    CONSTRAINT uq_metadata_type_title UNIQUE (content_type, normalized_title),

    -- Prevent duplicates based on external provider references
    CONSTRAINT uq_metadata_source_id UNIQUE (external_source, external_id)
);

CREATE INDEX idx_metadata_content_type ON content_metadata(content_type);
CREATE INDEX idx_metadata_normalized_title ON content_metadata(normalized_title);
CREATE INDEX idx_metadata_external_ref ON content_metadata(external_source, external_id);
