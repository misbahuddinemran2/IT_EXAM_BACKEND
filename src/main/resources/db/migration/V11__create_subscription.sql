-- V11__create_subscription.sql

CREATE TABLE subscription_plans (
    id                          VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    name                        VARCHAR(100)    NOT NULL,
    name_bn                     VARCHAR(100),
    plan_code                   VARCHAR(30)     NOT NULL UNIQUE,
    plan_type                   VARCHAR(10)     NOT NULL
                                    CHECK (plan_type IN ('FREE','MONTHLY','YEARLY','CUSTOM')),
    price_bdt                   DECIMAL(10,2)   NOT NULL DEFAULT 0,
    duration_days               INT             NOT NULL DEFAULT 30,
    description                 TEXT,
    max_practice_per_day        INT             NOT NULL DEFAULT 20,
    max_mock_tests_per_month    INT             NOT NULL DEFAULT 3,
    has_detailed_analytics      BOOLEAN         NOT NULL DEFAULT FALSE,
    has_weak_topic_detection    BOOLEAN         NOT NULL DEFAULT FALSE,
    has_leaderboard             BOOLEAN         NOT NULL DEFAULT FALSE,
    has_battle_exam             BOOLEAN         NOT NULL DEFAULT FALSE,
    has_written_exam            BOOLEAN         NOT NULL DEFAULT FALSE,
    has_ai_recommendation       BOOLEAN         NOT NULL DEFAULT FALSE,
    has_certificate             BOOLEAN         NOT NULL DEFAULT FALSE,
    has_offline_access          BOOLEAN         NOT NULL DEFAULT FALSE,
    has_live_exam               BOOLEAN         NOT NULL DEFAULT FALSE,
    is_popular                  BOOLEAN         NOT NULL DEFAULT FALSE,
    display_order               INT             NOT NULL DEFAULT 0,
    color_code                  VARCHAR(7)              DEFAULT '#6366f1',
    is_active                   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_plan_type   ON subscription_plans (plan_type);
CREATE INDEX idx_plan_active ON subscription_plans (is_active);


CREATE TABLE user_subscriptions (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    plan_id         VARCHAR(36)     NOT NULL,
    status          VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','EXPIRED','CANCELLED','TRIAL','PENDING')),
    payment_method  VARCHAR(20)     NOT NULL DEFAULT 'ADMIN_GRANTED'
                        CHECK (payment_method IN ('BKASH','NAGAD','ROCKET','CARD','BANK','ADMIN_GRANTED','PROMO_CODE','REFERRAL')),
    starts_at       TIMESTAMP       NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    auto_renew      BOOLEAN         NOT NULL DEFAULT FALSE,
    granted_by      VARCHAR(36),
    promo_code      VARCHAR(50),
    transaction_id  VARCHAR(200),
    amount_paid     DECIMAL(10,2)   NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2)   NOT NULL DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sub_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_sub_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

CREATE INDEX idx_sub_user    ON user_subscriptions (user_id);
CREATE INDEX idx_sub_status  ON user_subscriptions (status);
CREATE INDEX idx_sub_expires ON user_subscriptions (expires_at);


CREATE TABLE payment_transactions (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    subscription_id     VARCHAR(36),
    transaction_id      VARCHAR(200)    NOT NULL UNIQUE,
    payment_method      VARCHAR(20)     NOT NULL
                            CHECK (payment_method IN ('BKASH','NAGAD','ROCKET','CARD','BANK','ADMIN_GRANTED','PROMO_CODE','REFERRAL')),
    amount              DECIMAL(10,2)   NOT NULL,
    currency            VARCHAR(5)      NOT NULL DEFAULT 'BDT',
    status              VARCHAR(10)     NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED','CANCELLED')),
    gateway_response    JSONB,
    phone_number        VARCHAR(15),
    refund_amount       DECIMAL(10,2)   NOT NULL DEFAULT 0,
    refund_reason       TEXT,
    refunded_at         TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_payment_user   ON payment_transactions (user_id);
CREATE INDEX idx_payment_status ON payment_transactions (status);
CREATE INDEX idx_payment_txn    ON payment_transactions (transaction_id);


CREATE TABLE promo_codes (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    code                VARCHAR(50)     NOT NULL UNIQUE,
    discount_type       VARCHAR(15)     NOT NULL DEFAULT 'PERCENTAGE'
                            CHECK (discount_type IN ('PERCENTAGE','FIXED')),
    discount_value      DECIMAL(10,2)   NOT NULL,
    applicable_plan     VARCHAR(10)     NOT NULL DEFAULT 'ALL'
                            CHECK (applicable_plan IN ('MONTHLY','YEARLY','ALL')),
    max_uses            INT             NOT NULL DEFAULT 1,
    used_count          INT             NOT NULL DEFAULT 0,
    valid_from          TIMESTAMP       NOT NULL,
    valid_until         TIMESTAMP       NOT NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(36)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_promo_code   ON promo_codes (code);
CREATE INDEX idx_promo_active ON promo_codes (is_active);


INSERT INTO subscription_plans (
    id, name, name_bn, plan_code, plan_type,
    price_bdt, duration_days,
    max_practice_per_day, max_mock_tests_per_month,
    has_detailed_analytics, has_weak_topic_detection,
    has_leaderboard, has_battle_exam, has_written_exam,
    has_ai_recommendation, has_certificate,
    has_offline_access, has_live_exam,
    is_popular, display_order, color_code
) VALUES
(gen_random_uuid()::text,'Free Plan','ফ্রি প্ল্যান','FREE','FREE',
 0,36500,20,3,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,1,'#6b7280'),
(gen_random_uuid()::text,'Monthly Plan','মাসিক প্ল্যান','MONTHLY','MONTHLY',
 199,30,-1,-1,TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,2,'#3b82f6'),
(gen_random_uuid()::text,'Yearly Plan','বার্ষিক প্ল্যান','YEARLY','YEARLY',
 1499,365,-1,-1,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,3,'#8b5cf6');