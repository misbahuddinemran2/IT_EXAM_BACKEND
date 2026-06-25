-- V9__create_admin_users.sql

CREATE TABLE admin_users (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,

    role            VARCHAR(30)     NOT NULL DEFAULT 'CONTENT_MANAGER',

    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMP,

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT chk_admin_role
        CHECK (
            role IN (
                'SUPER_ADMIN',
                'CONTENT_MANAGER',
                'REVIEWER'
            )
        )
);

CREATE INDEX idx_admin_username ON admin_users (username);
CREATE INDEX idx_admin_email    ON admin_users (email);
CREATE INDEX idx_admin_active   ON admin_users (is_active);