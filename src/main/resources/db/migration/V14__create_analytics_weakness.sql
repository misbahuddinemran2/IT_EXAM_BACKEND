-- V14__create_analytics_weakness.sql

CREATE TABLE user_topic_weakness (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    topic_id            VARCHAR(36)     NOT NULL,
    exam_type_id        VARCHAR(36),
    total_attempts      INT             NOT NULL DEFAULT 0,
    correct_attempts    INT             NOT NULL DEFAULT 0,
    accuracy_rate       DECIMAL(5,2)    NOT NULL DEFAULT 0,
    weakness_score      DECIMAL(5,2)    NOT NULL DEFAULT 0.5,
    avg_time_spent_sec  DECIMAL(8,2)    NOT NULL DEFAULT 0,
    last_attempted_at   TIMESTAMP,
    last_computed_at    TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_weakness_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_user_topic_exam UNIQUE (user_id, topic_id, exam_type_id)
);

CREATE INDEX idx_weakness_user  ON user_topic_weakness (user_id);
CREATE INDEX idx_weakness_score ON user_topic_weakness (weakness_score);


CREATE TABLE user_concept_weakness (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    concept_id          VARCHAR(36)     NOT NULL,
    total_attempts      INT             NOT NULL DEFAULT 0,
    correct_attempts    INT             NOT NULL DEFAULT 0,
    accuracy_rate       DECIMAL(5,2)    NOT NULL DEFAULT 0,
    weakness_score      DECIMAL(5,2)    NOT NULL DEFAULT 0.5,
    last_computed_at    TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cweakness_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_user_concept UNIQUE (user_id, concept_id)
);

CREATE INDEX idx_cweakness_user  ON user_concept_weakness (user_id);
CREATE INDEX idx_cweakness_score ON user_concept_weakness (weakness_score);


CREATE TABLE user_performance_summary (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id                 VARCHAR(36)     NOT NULL UNIQUE,
    total_sessions          INT             NOT NULL DEFAULT 0,
    total_questions_seen    INT             NOT NULL DEFAULT 0,
    total_correct           INT             NOT NULL DEFAULT 0,
    overall_accuracy        DECIMAL(5,2)    NOT NULL DEFAULT 0,
    total_study_time_min    INT             NOT NULL DEFAULT 0,
    avg_score_per_exam      DECIMAL(8,2)    NOT NULL DEFAULT 0,
    best_score              DECIMAL(8,2)    NOT NULL DEFAULT 0,
    best_score_exam         VARCHAR(200),
    strongest_topic_id      VARCHAR(36),
    weakest_topic_id        VARCHAR(36),
    battles_played          INT             NOT NULL DEFAULT 0,
    battles_won             INT             NOT NULL DEFAULT 0,
    last_computed_at        TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_perf_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE user_misconceptions (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    question_id         VARCHAR(36)     NOT NULL,
    wrong_option_id     VARCHAR(36)     NOT NULL,
    select_count        INT             NOT NULL DEFAULT 1,
    concept_id          VARCHAR(36),
    last_occurred_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_misc_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_user_question_option UNIQUE (user_id, question_id, wrong_option_id)
);

CREATE INDEX idx_misc_user    ON user_misconceptions (user_id);
CREATE INDEX idx_misc_concept ON user_misconceptions (concept_id);


CREATE TABLE user_activity_logs (
    id              BIGSERIAL       NOT NULL,
    user_id         VARCHAR(36)     NOT NULL,
    activity_type   VARCHAR(30)     NOT NULL
                        CHECK (activity_type IN (
                            'LOGIN','LOGOUT','EXAM_STARTED','EXAM_COMPLETED',
                            'SUBSCRIPTION_PURCHASED','PASSWORD_CHANGED','PROFILE_UPDATED',
                            'BATTLE_STARTED','BATTLE_COMPLETED'
                        )),
    metadata        JSONB,
    ip_address      VARCHAR(45),
    device_type     VARCHAR(50),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_actlog_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_actlog_user ON user_activity_logs (user_id);
CREATE INDEX idx_actlog_type ON user_activity_logs (activity_type);
CREATE INDEX idx_actlog_date ON user_activity_logs (created_at);