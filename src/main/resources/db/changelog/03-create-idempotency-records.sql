-- liquibase formatted sql
-- changeset account:03-create-idempotency-records
CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    idem_key VARCHAR(255) NOT NULL UNIQUE,
    response_payload TEXT,
    http_status INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);
-- changeset account:03-create-idempotency-records-index
CREATE INDEX idx_idem_key ON idempotency_records(idem_key);