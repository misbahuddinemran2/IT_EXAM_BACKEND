-- V10__create_users.sql

CREATE TABLE users (
    id                      VARCHAR(36)         NOT NULL DEFAULT gen_random_uuid()::text,
    full_name               VARCHAR(100)        NOT NULL,
    full_name_bn            VARCHAR(100),
    email                   VARCHAR(100)        UNIQUE,
    phone                   VARCHAR(15)         UNIQUE,
    password_hash           VARCHAR(255),
    avatar_url              VARCHAR(500),
    date_of_birth           DATE,
    gender                  VARCHAR(10)         CHECK (gender IN ('MALE','FEMALE','OTHER')),
    district                VARCHAR(100),
    education_level         VARCHAR(10)         CHECK (education_level IN ('SSC','HSC','HONORS','MASTERS','OTHER')),
    target_exam             VARCHAR(100),
    is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
    is_email_verified       BOOLEAN             NOT NULL DEFAULT FALSE,
    is_phone_verified       BOOLEAN             NOT NULL DEFAULT FALSE,
    email_verify_token      VARCHAR(100),
    password_reset_token    VARCHAR(100),
    password_reset_expires  TIMESTAMP,
    last_login_at           TIMESTAMP,
    login_count             INT                 NOT NULL DEFAULT 0,
    referred_by             VARCHAR(36),
    created_at              TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_users_email        ON users (email);
CREATE INDEX idx_users_phone        ON users (phone);
CREATE INDEX idx_users_active       ON users (is_active);
CREATE INDEX idx_users_district     ON users (district);
CREATE INDEX idx_users_target_exam  ON users (target_exam);


CREATE TABLE user_devices (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    device_token    VARCHAR(500)    NOT NULL,
    device_type     VARCHAR(10)     NOT NULL DEFAULT 'ANDROID'
                        CHECK (device_type IN ('ANDROID','IOS','WEB')),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_used_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_device_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_device_user   ON user_devices (user_id);
CREATE INDEX idx_device_active ON user_devices (is_active);