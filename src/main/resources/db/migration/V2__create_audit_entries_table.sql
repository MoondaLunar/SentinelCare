CREATE TABLE audit_entries (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id BIGINT NOT NULL,
    details VARCHAR(4096),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
