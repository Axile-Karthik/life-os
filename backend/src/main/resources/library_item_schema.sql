-- Schema migration for creating library_item table.
-- Links content_metadata to user relationship status and activity tracking.

CREATE TABLE library_item (
    id UUID PRIMARY KEY,

    metadata_id UUID NOT NULL,

    status VARCHAR(50) NOT NULL,

    progress_percent INTEGER,

    rating DECIMAL(2,1),

    favorite BOOLEAN DEFAULT FALSE,

    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_activity_at TIMESTAMP,

    source_type VARCHAR(50),

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_library_metadata
        FOREIGN KEY (metadata_id)
        REFERENCES content_metadata(id)
);

CREATE INDEX idx_library_item_metadata ON library_item(metadata_id);
CREATE INDEX idx_library_item_status ON library_item(status);
