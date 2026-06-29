-- ============================================================
-- V19: App Version Management (PostgreSQL / Neon)
-- ============================================================

CREATE TABLE IF NOT EXISTS app_version (
    id               VARCHAR(36)  PRIMARY KEY DEFAULT gen_random_uuid()::text,
    app_version      VARCHAR(20)  NOT NULL,
    latest_version   VARCHAR(20)  NOT NULL,
    minimum_version  VARCHAR(20)  NOT NULL,
    force_update     BOOLEAN      NOT NULL DEFAULT FALSE,
    optional_update  BOOLEAN      NOT NULL DEFAULT FALSE,
    update_title     VARCHAR(255)          DEFAULT 'New Update Available',
    update_message   TEXT,
    android_apk_url  VARCHAR(500),
    ios_app_store_url VARCHAR(500),
    play_store_url   VARCHAR(500),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Default seed row
INSERT INTO app_version (
    id,
    app_version,
    latest_version,
    minimum_version,
    force_update,
    optional_update,
    update_title,
    update_message,
    android_apk_url,
    ios_app_store_url,
    play_store_url,
    is_active
) VALUES (
    gen_random_uuid()::text,
    '1.0.0',
    '1.0.0',
    '1.0.0',
    FALSE,
    FALSE,
    'New Update Available',
    'Performance improvements and new features added.',
    '',
    '',
    '',
    TRUE
);