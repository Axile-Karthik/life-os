-- Schema migration for project management dashboard (Single User).
-- Defines tables for projects, boards, columns, tasks, labels, comments, attachments, and activity logs.

CREATE TABLE project (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    ticket_prefix VARCHAR(10) NOT NULL,
    next_ticket_number BIGINT NOT NULL,
    deleted_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE board (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    project_id UUID NOT NULL,
    deleted_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_board_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE INDEX idx_board_project ON board(project_id);

CREATE TABLE board_column (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    board_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_column_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);

CREATE INDEX idx_column_board_pos ON board_column(board_id, position);

CREATE TABLE task (
    id UUID PRIMARY KEY,
    ticket_key VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    column_id UUID NOT NULL,
    position INTEGER NOT NULL,
    priority VARCHAR(50) NOT NULL,
    due_date TIMESTAMP,
    deleted_at TIMESTAMP,
    parent_id UUID,
    version INTEGER NOT NULL DEFAULT 0,
    search_vector tsvector,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_task_column FOREIGN KEY (column_id) REFERENCES board_column(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_parent FOREIGN KEY (parent_id) REFERENCES task(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_column_pos ON task(column_id, position);
CREATE INDEX idx_task_search ON task USING GIN(search_vector);

-- Trigger function to auto-update the search_vector on every INSERT or UPDATE
CREATE OR REPLACE FUNCTION task_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', coalesce(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.description, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(NEW.ticket_key, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_task_search_vector
    BEFORE INSERT OR UPDATE OF title, description, ticket_key
    ON task
    FOR EACH ROW
    EXECUTE FUNCTION task_search_vector_update();

CREATE TABLE label (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    project_id UUID NOT NULL,
    CONSTRAINT fk_label_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_label UNIQUE (project_id, name)
);

CREATE TABLE task_labels (
    task_id UUID NOT NULL,
    label_id UUID NOT NULL,
    PRIMARY KEY (task_id, label_id),
    CONSTRAINT fk_tl_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_tl_label FOREIGN KEY (label_id) REFERENCES label(id) ON DELETE CASCADE
);

CREATE TABLE task_comment (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    task_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_task ON task_comment(task_id);

CREATE TABLE task_attachment (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    task_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

CREATE INDEX idx_attachment_task ON task_attachment(task_id);

CREATE TABLE activity_log (
    id UUID PRIMARY KEY,
    project_id UUID,
    board_id UUID,
    task_id UUID,
    action_type VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL
);

CREATE INDEX idx_activity_project ON activity_log(project_id);
