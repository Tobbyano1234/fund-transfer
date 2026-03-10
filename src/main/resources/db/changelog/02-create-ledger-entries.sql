-- liquibase formatted sql
-- changeset account:02-create-ledger-entries
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    related_account_id UUID,
    balance_after BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
-- changeset account:02-create-ledger-entries-indexes
CREATE INDEX idx_ledger_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_idempotency ON ledger_entries(idempotency_key);
CREATE INDEX idx_ledger_created_at ON ledger_entries(created_at DESC);
-- changeset account:02-create-ledger-entries-fk
ALTER TABLE ledger_entries
ADD CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES accounts(id);