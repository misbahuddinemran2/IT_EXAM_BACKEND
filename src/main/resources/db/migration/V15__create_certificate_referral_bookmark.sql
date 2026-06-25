-- V15__create_certificate_referral_bookmark.sql

CREATE TABLE certificates (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id             VARCHAR(36)     NOT NULL,
    session_id          VARCHAR(36)     NOT NULL,
    exam_type_id        VARCHAR(36),
    special_exam_id     VARCHAR(36),
    certificate_number  VARCHAR(50)     NOT NULL UNIQUE,
    title               VARCHAR(200)    NOT NULL,
    score               DECIMAL(8,2)    NOT NULL,
    percentage          DECIMAL(5,2)    NOT NULL,
    grade               VARCHAR(10),
    issued_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at          TIMESTAMP,
    pdf_url             VARCHAR(500),
    is_valid            BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cert_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cert_session
        FOREIGN KEY (session_id) REFERENCES user_exam_sessions(id)
);

CREATE INDEX idx_cert_user   ON certificates (user_id);
CREATE INDEX idx_cert_number ON certificates (certificate_number);


CREATE TABLE referrals (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    referrer_user_id        VARCHAR(36)     NOT NULL,
    referred_user_id        VARCHAR(36)     NOT NULL,
    referral_code           VARCHAR(20)     NOT NULL,
    status                  VARCHAR(15)     NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING','COMPLETED','REWARD_GIVEN')),
    referrer_reward_value   DECIMAL(10,2)   NOT NULL DEFAULT 7,
    referred_reward_value   DECIMAL(10,2)   NOT NULL DEFAULT 7,
    completed_at            TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ref_referrer
        FOREIGN KEY (referrer_user_id) REFERENCES users(id),
    CONSTRAINT fk_ref_referred
        FOREIGN KEY (referred_user_id) REFERENCES users(id),
    CONSTRAINT uq_referred UNIQUE (referred_user_id)
);

CREATE INDEX idx_ref_referrer ON referrals (referrer_user_id);
CREATE INDEX idx_ref_code     ON referrals (referral_code);
CREATE INDEX idx_ref_status   ON referrals (status);


CREATE TABLE user_referral_codes (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id                 VARCHAR(36)     NOT NULL UNIQUE,
    code                    VARCHAR(20)     NOT NULL UNIQUE,
    total_referrals         INT             NOT NULL DEFAULT 0,
    successful_referrals    INT             NOT NULL DEFAULT 0,
    total_rewards           DECIMAL(10,2)   NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_refcode_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refcode_code ON user_referral_codes (code);


CREATE TABLE question_bookmarks (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    question_id     VARCHAR(36)     NOT NULL,
    note            TEXT,
    folder          VARCHAR(100)    NOT NULL DEFAULT 'Default',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bookmark_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookmark_question
        FOREIGN KEY (question_id) REFERENCES questions(id),
    CONSTRAINT uq_user_question UNIQUE (user_id, question_id)
);

CREATE INDEX idx_bookmark_user   ON question_bookmarks (user_id);
CREATE INDEX idx_bookmark_folder ON question_bookmarks (user_id, folder);


CREATE TABLE bookmark_folders (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    color_code      VARCHAR(7)              DEFAULT '#6366f1',
    question_count  INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_folder_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_user_folder UNIQUE (user_id, name)
);

CREATE INDEX idx_folder_user ON bookmark_folders (user_id);


CREATE TABLE user_question_reports (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    question_id     VARCHAR(36)     NOT NULL,
    report_type     VARCHAR(15)     NOT NULL
                        CHECK (report_type IN (
                            'WRONG_ANSWER','TYPO','OUTDATED',
                            'AMBIGUOUS','DUPLICATE','INAPPROPRIATE'
                        )),
    description     TEXT,
    status          VARCHAR(15)     NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN','UNDER_REVIEW','RESOLVED','DISMISSED')),
    resolved_by     VARCHAR(36),
    resolution_note TEXT,
    notified_user   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_report_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_report_question
        FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE INDEX idx_report_user     ON user_question_reports (user_id);
CREATE INDEX idx_report_question ON user_question_reports (question_id);
CREATE INDEX idx_report_status   ON user_question_reports (status);


CREATE TABLE user_friends (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36)     NOT NULL,
    friend_id       VARCHAR(36)     NOT NULL,
    status          VARCHAR(10)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','ACCEPTED','BLOCKED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_friend_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_friend_friend
        FOREIGN KEY (friend_id) REFERENCES users(id),
    CONSTRAINT uq_friendship UNIQUE (user_id, friend_id)
);

CREATE INDEX idx_friend_user   ON user_friends (user_id);
CREATE INDEX idx_friend_status ON user_friends (status);