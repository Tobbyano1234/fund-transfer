-- liquibase formatted sql
-- changeset account:01-create-accounts
CREATE TABLE Accounts (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
-- changeset account:01-create-accounts-index
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_owner ON accounts(owner_name);
-- changeset account:01-create-accounts-unique
ALTER TABLE accounts
ADD CONSTRAINT uk_accounts_owner_currency UNIQUE (owner_id, currency);