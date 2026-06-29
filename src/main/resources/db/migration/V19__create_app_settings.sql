-- ============================================================
-- V18: App Settings (PostgreSQL / Neon)
-- ============================================================

CREATE TABLE IF NOT EXISTS app_settings (
    id            VARCHAR(36)  PRIMARY KEY DEFAULT gen_random_uuid()::text,
    setting_key   VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type  VARCHAR(50)  NOT NULL DEFAULT 'STRING',
    category      VARCHAR(50)  NOT NULL DEFAULT 'GENERAL',
    description   VARCHAR(255),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── MAINTENANCE ───────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'maintenance_mode',            'false',                                                              'BOOLEAN', 'MAINTENANCE', 'Enable/disable maintenance mode'),
(gen_random_uuid()::text, 'maintenance_title',           'Under Maintenance',                                                  'STRING',  'MAINTENANCE', 'Maintenance page title'),
(gen_random_uuid()::text, 'maintenance_message',         'We are performing scheduled maintenance. Please try again later.',   'STRING',  'MAINTENANCE', 'Maintenance message shown to users'),
(gen_random_uuid()::text, 'maintenance_estimated_time',  '',                                                                   'STRING',  'MAINTENANCE', 'Estimated end time e.g. 2:00 PM');

-- ── SPLASH ────────────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'splash_title',       'IT Exam Guru',                 'STRING',  'SPLASH', 'Splash screen app title'),
(gen_random_uuid()::text, 'splash_subtitle',    'Prepare. Practice. Succeed.',  'STRING',  'SPLASH', 'Splash screen subtitle'),
(gen_random_uuid()::text, 'splash_image_url',   '',                             'STRING',  'SPLASH', 'Splash image Firebase Storage URL'),
(gen_random_uuid()::text, 'splash_duration_ms', '2000',                         'INTEGER', 'SPLASH', 'Splash display duration in milliseconds');

-- ── NOTICE POPUP ──────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'notice_popup_enabled',     'false',  'BOOLEAN', 'NOTICE_POPUP', 'Enable notice popup on startup'),
(gen_random_uuid()::text, 'notice_popup_title',       '',       'STRING',  'NOTICE_POPUP', 'Notice popup title'),
(gen_random_uuid()::text, 'notice_popup_message',     '',       'STRING',  'NOTICE_POPUP', 'Notice popup body message'),
(gen_random_uuid()::text, 'notice_popup_image_url',   '',       'STRING',  'NOTICE_POPUP', 'Notice popup image Firebase URL'),
(gen_random_uuid()::text, 'notice_popup_button_text', 'Got it', 'STRING',  'NOTICE_POPUP', 'Notice popup dismiss button text'),
(gen_random_uuid()::text, 'notice_popup_link_url',    '',       'STRING',  'NOTICE_POPUP', 'Optional external link URL');

-- ── HOME BANNER ───────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'home_banner_enabled',   'true', 'BOOLEAN', 'HOME_BANNER', 'Enable home screen banner'),
(gen_random_uuid()::text, 'home_banner_image_url', '',     'STRING',  'HOME_BANNER', 'Banner image Firebase Storage URL'),
(gen_random_uuid()::text, 'home_banner_link_url',  '',     'STRING',  'HOME_BANNER', 'Banner click destination URL');

-- ── HOME POPUP ────────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'home_popup_enabled',   'false', 'BOOLEAN', 'HOME_POPUP', 'Enable home popup after login'),
(gen_random_uuid()::text, 'home_popup_title',     '',      'STRING',  'HOME_POPUP', 'Home popup title'),
(gen_random_uuid()::text, 'home_popup_message',   '',      'STRING',  'HOME_POPUP', 'Home popup message'),
(gen_random_uuid()::text, 'home_popup_image_url', '',      'STRING',  'HOME_POPUP', 'Home popup image Firebase URL');

-- ── EXAM NOTICE ───────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'exam_notice_enabled', 'false', 'BOOLEAN', 'EXAM_NOTICE', 'Enable exam notice banner'),
(gen_random_uuid()::text, 'exam_notice_message', '',      'STRING',  'EXAM_NOTICE', 'Exam notice text shown on exam list');

-- ── FEATURES ──────────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'live_exam_enabled',            'false', 'BOOLEAN', 'FEATURES', 'Enable live/battle exam feature'),
(gen_random_uuid()::text, 'registration_enabled',         'true',  'BOOLEAN', 'FEATURES', 'Allow new user registrations'),
(gen_random_uuid()::text, 'guest_mode_enabled',           'false', 'BOOLEAN', 'FEATURES', 'Allow unauthenticated guest browsing'),
(gen_random_uuid()::text, 'leaderboard_enabled',          'true',  'BOOLEAN', 'FEATURES', 'Show leaderboard feature'),
(gen_random_uuid()::text, 'premium_subscription_enabled', 'true',  'BOOLEAN', 'FEATURES', 'Enable premium subscription purchases'),
(gen_random_uuid()::text, 'ads_enabled',                  'false', 'BOOLEAN', 'FEATURES', 'Show ads in the app');

-- ── SUPPORT ───────────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'support_email',    'support@itexamguru.com', 'STRING', 'SUPPORT', 'Support email address'),
(gen_random_uuid()::text, 'support_phone',    '',                       'STRING', 'SUPPORT', 'Support phone number'),
(gen_random_uuid()::text, 'support_whatsapp', '',                       'STRING', 'SUPPORT', 'Support WhatsApp number');

-- ── SOCIAL LINKS ──────────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'facebook_page_url',   '',                       'STRING', 'SOCIAL', 'Facebook page URL'),
(gen_random_uuid()::text, 'facebook_group_url',  '',                       'STRING', 'SOCIAL', 'Facebook group URL'),
(gen_random_uuid()::text, 'youtube_channel_url', '',                       'STRING', 'SOCIAL', 'YouTube channel URL'),
(gen_random_uuid()::text, 'telegram_group_url',  '',                       'STRING', 'SOCIAL', 'Telegram group URL'),
(gen_random_uuid()::text, 'website_url',         'https://itexamguru.com', 'STRING', 'SOCIAL', 'Main website URL');

-- ── DOWNLOAD / SYSTEM ─────────────────────────────────────────
INSERT INTO app_settings (id, setting_key, setting_value, setting_type, category, description) VALUES
(gen_random_uuid()::text, 'android_apk_download_url', '', 'STRING', 'DOWNLOAD', 'Direct APK download URL (outside Play Store)'),
(gen_random_uuid()::text, 'system_status', 'OPERATIONAL',             'STRING', 'SYSTEM',   'System status: OPERATIONAL | DEGRADED | DOWN');