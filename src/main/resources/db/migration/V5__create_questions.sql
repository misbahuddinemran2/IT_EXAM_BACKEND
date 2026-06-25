-- V5__create_questions.sql

CREATE TABLE questions (
    id                      VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    question_text           TEXT            NOT NULL,
    question_text_bn        TEXT,
    question_type           VARCHAR(15)     NOT NULL DEFAULT 'MCQ_SINGLE'
                                CHECK (question_type IN ('MCQ_SINGLE','MCQ_MULTI','TRUE_FALSE')),
    language                VARCHAR(5)      NOT NULL DEFAULT 'EN'
                                CHECK (language IN ('EN','BN','BOTH')),
    subject_id              VARCHAR(36)     NOT NULL,
    chapter_id              VARCHAR(36)     NOT NULL,
    topic_id                VARCHAR(36)     NOT NULL,
    difficulty_level        SMALLINT        NOT NULL DEFAULT 3,
    cognitive_level         VARCHAR(15)     NOT NULL DEFAULT 'REMEMBER'
                                CHECK (cognitive_level IN (
                                    'REMEMBER','UNDERSTAND','APPLY','ANALYZE','EVALUATE'
                                )),
    estimated_time_sec      SMALLINT        NOT NULL DEFAULT 60,
    source_reference        VARCHAR(500),
    year_appeared           SMALLINT,
    is_reusable             BOOLEAN         NOT NULL DEFAULT TRUE,
    status                  VARCHAR(15)     NOT NULL DEFAULT 'DRAFT'
                                CHECK (status IN (
                                    'DRAFT','UNDER_REVIEW','APPROVED','REJECTED','ARCHIVED'
                                )),
    review_notes            TEXT,
    reported_count          SMALLINT        NOT NULL DEFAULT 0,
    content_hash            VARCHAR(64),
    ai_generated            BOOLEAN         NOT NULL DEFAULT FALSE,
    ai_confidence_score     DECIMAL(3,2),
    embedding_vector        TEXT,
    version                 INT             NOT NULL DEFAULT 1,
    created_by              VARCHAR(36)     NOT NULL,
    reviewed_by             VARCHAR(36),
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_content_hash
        UNIQUE (content_hash),
    CONSTRAINT fk_questions_subject
        FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

CREATE INDEX idx_q_status           ON questions (status);
CREATE INDEX idx_q_subject_status   ON questions (subject_id, status);
CREATE INDEX idx_q_topic_difficulty ON questions (topic_id, difficulty_level, status);
CREATE INDEX idx_q_year             ON questions (year_appeared);
CREATE INDEX idx_q_type_status      ON questions (question_type, status);
CREATE INDEX idx_q_created_by       ON questions (created_by);
CREATE INDEX idx_q_ai_generated     ON questions (ai_generated);