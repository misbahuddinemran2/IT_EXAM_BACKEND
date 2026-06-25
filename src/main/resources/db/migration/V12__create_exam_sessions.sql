-- V12__create_exam_sessions.sql

CREATE TABLE special_exams (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    title                   VARCHAR(200)    NOT NULL,
    title_bn                VARCHAR(200),
    description             TEXT,
    exam_category           VARCHAR(10)     NOT NULL DEFAULT 'MOCK_TEST'
                                CHECK (exam_category IN ('MOCK_TEST','BATTLE','WRITTEN','LIVE','CUSTOM','CHALLENGE')),
    exam_type_id            VARCHAR(36),
    required_plan           VARCHAR(10)     NOT NULL DEFAULT 'FREE'
                                CHECK (required_plan IN ('FREE','MONTHLY','YEARLY')),
    total_questions         INT             NOT NULL DEFAULT 30,
    time_limit_minutes      INT             NOT NULL DEFAULT 30,
    negative_marking        BOOLEAN         NOT NULL DEFAULT FALSE,
    negative_value          DECIMAL(4,2)    NOT NULL DEFAULT 0.25,
    pass_percentage         DECIMAL(5,2)    NOT NULL DEFAULT 40,
    max_participants        INT,
    is_scheduled            BOOLEAN         NOT NULL DEFAULT FALSE,
    scheduled_at            TIMESTAMP,
    ends_at                 TIMESTAMP,
    show_leaderboard        BOOLEAN         NOT NULL DEFAULT TRUE,
    show_result_instantly   BOOLEAN         NOT NULL DEFAULT TRUE,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(36)     NOT NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sexam_examtype
        FOREIGN KEY (exam_type_id) REFERENCES exam_types(id)
);

CREATE INDEX idx_sexam_category ON special_exams (exam_category);
CREATE INDEX idx_sexam_plan     ON special_exams (required_plan);
CREATE INDEX idx_sexam_active   ON special_exams (is_active);


CREATE TABLE user_exam_sessions (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    special_exam_id     VARCHAR(36),
    exam_type_id        VARCHAR(36),
    topic_id            VARCHAR(36),
    session_type        VARCHAR(10)     NOT NULL DEFAULT 'PRACTICE'
                            CHECK (session_type IN ('MOCK','PRACTICE','TOPIC_WISE','BATTLE','WRITTEN','LIVE','CUSTOM','CHALLENGE')),
    status              VARCHAR(15)     NOT NULL DEFAULT 'IN_PROGRESS'
                            CHECK (status IN ('IN_PROGRESS','COMPLETED','ABANDONED','TIMED_OUT')),
    total_questions     INT             NOT NULL DEFAULT 0,
    attempted_count     INT             NOT NULL DEFAULT 0,
    correct_count       INT             NOT NULL DEFAULT 0,
    wrong_count         INT             NOT NULL DEFAULT 0,
    skip_count          INT             NOT NULL DEFAULT 0,
    score               DECIMAL(8,2)    NOT NULL DEFAULT 0,
    percentage          DECIMAL(5,2)    NOT NULL DEFAULT 0,
    time_spent_sec      INT             NOT NULL DEFAULT 0,
    is_passed           BOOLEAN         NOT NULL DEFAULT FALSE,
    rank_in_exam        INT,
    percentile          DECIMAL(5,2),
    started_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_usession_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_usession_sexam
        FOREIGN KEY (special_exam_id) REFERENCES special_exams(id),
    CONSTRAINT fk_usession_examtype
        FOREIGN KEY (exam_type_id) REFERENCES exam_types(id)
);

CREATE INDEX idx_usession_user   ON user_exam_sessions (user_id);
CREATE INDEX idx_usession_status ON user_exam_sessions (status);
CREATE INDEX idx_usession_type   ON user_exam_sessions (session_type);


CREATE TABLE user_question_attempts (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    session_id          VARCHAR(36)     NOT NULL,
    question_id         VARCHAR(36)     NOT NULL,
    selected_option_id  VARCHAR(36),
    is_correct          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_skipped          BOOLEAN         NOT NULL DEFAULT FALSE,
    time_spent_sec      INT             NOT NULL DEFAULT 0,
    confidence_level    SMALLINT,
    answered_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_uattempt_session
        FOREIGN KEY (session_id) REFERENCES user_exam_sessions(id),
    CONSTRAINT fk_uattempt_question
        FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE INDEX idx_uattempt_session  ON user_question_attempts (session_id);
CREATE INDEX idx_uattempt_question ON user_question_attempts (question_id);


CREATE TABLE battle_rooms (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    special_exam_id     VARCHAR(36)     NOT NULL,
    user_id_1           VARCHAR(36)     NOT NULL,
    user_id_2           VARCHAR(36),
    session_id_1        VARCHAR(36),
    session_id_2        VARCHAR(36),
    status              VARCHAR(15)     NOT NULL DEFAULT 'WAITING'
                            CHECK (status IN ('WAITING','IN_PROGRESS','COMPLETED','CANCELLED','EXPIRED')),
    winner_user_id      VARCHAR(36),
    room_code           VARCHAR(10)     UNIQUE,
    is_private          BOOLEAN         NOT NULL DEFAULT FALSE,
    expires_at          TIMESTAMP,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_battle_sexam
        FOREIGN KEY (special_exam_id) REFERENCES special_exams(id),
    CONSTRAINT fk_battle_user1
        FOREIGN KEY (user_id_1) REFERENCES users(id),
    CONSTRAINT fk_battle_user2
        FOREIGN KEY (user_id_2) REFERENCES users(id)
);

CREATE INDEX idx_battle_status    ON battle_rooms (status);
CREATE INDEX idx_battle_room_code ON battle_rooms (room_code);


CREATE TABLE written_answers (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    session_id          VARCHAR(36)     NOT NULL,
    question_id         VARCHAR(36)     NOT NULL,
    answer_text         TEXT            NOT NULL,
    word_count          INT             NOT NULL DEFAULT 0,
    ai_score            DECIMAL(5,2),
    ai_feedback         TEXT,
    ai_verified_at      TIMESTAMP,
    manual_score        DECIMAL(5,2),
    manual_feedback     TEXT,
    reviewed_by         VARCHAR(36),
    reviewed_at         TIMESTAMP,
    final_score         DECIMAL(5,2),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_written_session
        FOREIGN KEY (session_id) REFERENCES user_exam_sessions(id)
);

CREATE INDEX idx_written_session  ON written_answers (session_id);
CREATE INDEX idx_written_question ON written_answers (question_id);


CREATE TABLE leaderboard (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    exam_type_id    VARCHAR(36),
    special_exam_id VARCHAR(36),
    period_type     VARCHAR(10)     NOT NULL DEFAULT 'ALL_TIME'
                        CHECK (period_type IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME')),
    total_score     DECIMAL(10,2)   NOT NULL DEFAULT 0,
    total_sessions  INT             NOT NULL DEFAULT 0,
    total_attempts  INT             NOT NULL DEFAULT 0,
    accuracy_rate   DECIMAL(5,2)    NOT NULL DEFAULT 0,
    rank_position   INT,
    percentile      DECIMAL(5,2),
    period_start    DATE,
    computed_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_leader_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_leader_examtype
        FOREIGN KEY (exam_type_id) REFERENCES exam_types(id),
    CONSTRAINT fk_leader_sexam
        FOREIGN KEY (special_exam_id) REFERENCES special_exams(id)
);

CREATE INDEX idx_leader_examtype ON leaderboard (exam_type_id, period_type);
CREATE INDEX idx_leader_sexam    ON leaderboard (special_exam_id, period_type);
CREATE INDEX idx_leader_user     ON leaderboard (user_id);
CREATE INDEX idx_leader_rank     ON leaderboard (rank_position);