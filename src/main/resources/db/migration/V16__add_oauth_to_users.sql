-- V16__add_oauth_to_users.sql

-- email nullable
ALTER TABLE users
    ALTER COLUMN email DROP NOT NULL;

-- password nullable
ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

-- OAuth columns
ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(10) NOT NULL DEFAULT 'LOCAL'
        CHECK (auth_provider IN ('LOCAL','GOOGLE','FACEBOOK')),
    ADD COLUMN provider_id VARCHAR(255) NULL;

CREATE INDEX idx_users_auth_provider ON users (auth_provider);
CREATE INDEX idx_users_provider_id   ON users (provider_id);