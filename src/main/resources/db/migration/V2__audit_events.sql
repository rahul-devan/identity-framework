SET search_path TO identity_framework;

CREATE TABLE IF NOT EXISTS audit_events (
    id            BIGSERIAL PRIMARY KEY,
    action        VARCHAR(255) NOT NULL,
    entity_type   VARCHAR(255) NOT NULL,
    entity_id     BIGINT,
    actor_user_id BIGINT,
    details       TEXT,
    occurred_at   TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_occurred_at ON audit_events(occurred_at);
