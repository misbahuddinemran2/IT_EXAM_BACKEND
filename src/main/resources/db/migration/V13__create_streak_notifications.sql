-- V13__create_streak_notifications.sql

CREATE TABLE study_streaks (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id                 VARCHAR(36)     NOT NULL UNIQUE,
    current_streak_days     INT             NOT NULL DEFAULT 0,
    longest_streak_days     INT             NOT NULL DEFAULT 0,
    last_activity_date      DATE,
    streak_freeze_count     INT             NOT NULL DEFAULT 2,
    total_study_days        INT             NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_streak_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE user_badges (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    badge_type      VARCHAR(20)     NOT NULL
                        CHECK (badge_type IN (
                            'STREAK_7','STREAK_30','STREAK_100',
                            'FIRST_EXAM','PERFECT_SCORE','TOP_10',
                            'TOP_1','BATTLE_WINNER','FAST_LEARNER','CONSISTENT'
                        )),
    earned_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_badge_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_user_badge UNIQUE (user_id, badge_type)
);

CREATE INDEX idx_badge_user ON user_badges (user_id);


CREATE TABLE user_notifications (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    type                VARCHAR(30)     NOT NULL
                            CHECK (type IN (
                                'EXAM_REMINDER','WEAK_TOPIC_ALERT','NEW_EXAM_AVAILABLE',
                                'STREAK_BROKEN','STREAK_MILESTONE','RANK_CHANGED',
                                'EXAM_RESULT','SUBSCRIPTION_EXPIRING','SUBSCRIPTION_EXPIRED',
                                'PAYMENT_SUCCESS','BATTLE_INVITE','BATTLE_RESULT',
                                'BADGE_EARNED','SYSTEM'
                            )),
    title               VARCHAR(200)    NOT NULL,
    body                TEXT            NOT NULL,
    metadata            JSONB,
    is_read             BOOLEAN         NOT NULL DEFAULT FALSE,
    delivery_channel    VARCHAR(10)     NOT NULL DEFAULT 'IN_APP'
                            CHECK (delivery_channel IN ('IN_APP','EMAIL','SMS','PUSH')),
    sent_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at             TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notif_user ON user_notifications (user_id);
CREATE INDEX idx_notif_read ON user_notifications (user_id, is_read);
CREATE INDEX idx_notif_type ON user_notifications (type);


CREATE TABLE study_plans (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    exam_type_id        VARCHAR(36)     NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    target_exam_date    DATE            NOT NULL,
    daily_study_minutes INT             NOT NULL DEFAULT 30,
    status              VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE','COMPLETED','PAUSED','ABANDONED')),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_plan_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_plan_examtype
        FOREIGN KEY (exam_type_id) REFERENCES exam_types(id)
);

CREATE INDEX idx_plan_user   ON study_plans (user_id);
CREATE INDEX idx_plan_status ON study_plans (status);


CREATE TABLE study_plan_topics (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    plan_id                 VARCHAR(36)     NOT NULL,
    topic_id                VARCHAR(36)     NOT NULL,
    target_questions_count  INT             NOT NULL DEFAULT 20,
    completed_count         INT             NOT NULL DEFAULT 0,
    scheduled_date          DATE,
    completion_status       VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                                CHECK (completion_status IN ('PENDING','IN_PROGRESS','DONE','SKIPPED')),
    priority_score          DECIMAL(5,2)    NOT NULL DEFAULT 0.5,
    PRIMARY KEY (id),
    CONSTRAINT fk_spt_plan
        FOREIGN KEY (plan_id) REFERENCES study_plans(id)
);

CREATE INDEX idx_spt_plan   ON study_plan_topics (plan_id);
CREATE INDEX idx_spt_status ON study_plan_topics (completion_status);