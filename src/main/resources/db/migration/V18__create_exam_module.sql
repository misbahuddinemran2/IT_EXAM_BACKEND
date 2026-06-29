-- V18__create_exam_module.sql

-- ============================================
-- 1. MAIN EXAM TABLE
-- ============================================
CREATE TABLE exams (
    id                      VARCHAR(36)     NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    exam_code               VARCHAR(50),
    exam_type               VARCHAR(30)     NOT NULL
                                CHECK (exam_type IN (
                                    'DAILY','WEEKLY','REVISION',
                                    'SUBJECT_WISE','CHAPTER_WISE',
                                    'TOPIC_WISE','MIXED','SPECIAL'
                                )),
    publish_status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT'
                                CHECK (publish_status IN ('DRAFT','PUBLISHED','ARCHIVED')),

    -- Marks & Duration
    total_questions         INT             NOT NULL DEFAULT 0,
    total_marks             DECIMAL(8,2)    NOT NULL,
    pass_marks              DECIMAL(8,2)    NOT NULL,
    negative_marking        DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
    duration_minutes        INT             NOT NULL,

    -- Schedule
    exam_date               DATE            NOT NULL,
    start_time              TIME            NOT NULL,
    end_time                TIME            NOT NULL,

    -- Attempt Control (NULL = unlimited)
    max_attempts            INT             DEFAULT NULL,

    -- Settings
    allow_review            BOOLEAN         NOT NULL DEFAULT TRUE,
    shuffle_questions       BOOLEAN         NOT NULL DEFAULT FALSE,
    shuffle_options         BOOLEAN         NOT NULL DEFAULT FALSE,
    show_result_after_submit BOOLEAN        NOT NULL DEFAULT TRUE,

    -- Access
    is_premium_only         BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Description
    description             TEXT,

    -- Audit
    created_by              VARCHAR(36)     NOT NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_exam_code UNIQUE (exam_code)
);

-- updated_at auto-update করার জন্য function + trigger
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_exams_updated_at ON exams;
CREATE TRIGGER trg_exams_updated_at
BEFORE UPDATE ON exams
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================
-- 2. EXAM SUBJECT CONFIG
-- কোন subject থেকে কত question, কোন pattern
-- ============================================
CREATE TABLE exam_subject_configs (
    id                      VARCHAR(36)     NOT NULL,
    exam_id                 VARCHAR(36)     NOT NULL,
    subject_id              VARCHAR(36)     NOT NULL,
    question_count          INT             NOT NULL DEFAULT 0,
    marks_per_question      DECIMAL(5,2)    NOT NULL DEFAULT 1.00,

    -- Difficulty % (তিনটা মিলে 100 হবে)
    easy_percent            INT             NOT NULL DEFAULT 0,
    medium_percent          INT             NOT NULL DEFAULT 0,
    hard_percent            INT             NOT NULL DEFAULT 0,

    -- Cognitive % (মিলে 100 হবে)
    remember_percent        INT             NOT NULL DEFAULT 0,
    understand_percent      INT             NOT NULL DEFAULT 0,
    apply_percent           INT             NOT NULL DEFAULT 0,
    analyze_percent         INT             NOT NULL DEFAULT 0,
    evaluate_percent        INT             NOT NULL DEFAULT 0,

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_esc_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_esc_subject
        FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- ============================================
-- 3. EXAM TOPIC CONFIG
-- Mixed exam এ specific topic level control
-- ============================================
CREATE TABLE exam_topic_configs (
    id                      VARCHAR(36)     NOT NULL,
    exam_id                 VARCHAR(36)     NOT NULL,
    subject_id              VARCHAR(36)     NOT NULL,
    chapter_id              VARCHAR(36),
    topic_id                VARCHAR(36),
    question_count          INT             NOT NULL DEFAULT 0,
    marks_per_question      DECIMAL(5,2)    NOT NULL DEFAULT 1.00,

    -- Difficulty %
    easy_percent            INT             NOT NULL DEFAULT 0,
    medium_percent          INT             NOT NULL DEFAULT 0,
    hard_percent            INT             NOT NULL DEFAULT 0,

    -- Cognitive %
    remember_percent        INT             NOT NULL DEFAULT 0,
    understand_percent      INT             NOT NULL DEFAULT 0,
    apply_percent           INT             NOT NULL DEFAULT 0,
    analyze_percent         INT             NOT NULL DEFAULT 0,
    evaluate_percent        INT             NOT NULL DEFAULT 0,

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_etc_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

-- ============================================
-- 4. EXAM QUESTIONS (pre-selected or auto-selected)
-- ============================================
CREATE TABLE exam_questions (
    id                      VARCHAR(36)     NOT NULL,
    exam_id                 VARCHAR(36)     NOT NULL,
    question_id             VARCHAR(36)     NOT NULL,
    marks                   DECIMAL(5,2)    NOT NULL DEFAULT 1.00,
    order_number            INT             NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_eq_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_eq_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT uk_exam_question
        UNIQUE (exam_id, question_id)
);

-- ============================================
-- 5. EXAM ATTEMPT HISTORY
-- কে কতবার attempt করেছে
-- ============================================
CREATE TABLE exam_attempt_history (
    id                      VARCHAR(36)     NOT NULL,
    user_id                 VARCHAR(36)     NOT NULL,
    exam_id                 VARCHAR(36)     NOT NULL,
    session_id              VARCHAR(36)     NOT NULL,
    attempt_number          INT             NOT NULL DEFAULT 1,
    obtained_marks          DECIMAL(8,2)    NOT NULL DEFAULT 0,
    total_marks             DECIMAL(8,2)    NOT NULL DEFAULT 0,
    percentage              DECIMAL(5,2)    NOT NULL DEFAULT 0,
    is_passed               BOOLEAN         NOT NULL DEFAULT FALSE,
    submitted_at            TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_eah_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

-- ============================================
-- INDEXES
-- ============================================
CREATE INDEX idx_exams_type         ON exams(exam_type);
CREATE INDEX idx_exams_status       ON exams(publish_status);
CREATE INDEX idx_exams_date         ON exams(exam_date);
CREATE INDEX idx_exams_created_by   ON exams(created_by);

CREATE INDEX idx_esc_exam           ON exam_subject_configs(exam_id);
CREATE INDEX idx_etc_exam           ON exam_topic_configs(exam_id);
CREATE INDEX idx_eq_exam            ON exam_questions(exam_id);
CREATE INDEX idx_eah_user_exam      ON exam_attempt_history(user_id, exam_id);
CREATE INDEX idx_eah_exam           ON exam_attempt_history(exam_id);