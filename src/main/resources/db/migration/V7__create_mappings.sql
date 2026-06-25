-- V7__create_mappings.sql

CREATE TABLE question_concepts (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    question_id     VARCHAR(36)     NOT NULL,
    concept_id      VARCHAR(36)     NOT NULL,
    weight          DECIMAL(3,2)    NOT NULL DEFAULT 1.00,
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_qc_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    CONSTRAINT fk_qc_concept
        FOREIGN KEY (concept_id) REFERENCES concepts(id) ON DELETE CASCADE,

    CONSTRAINT uq_question_concept
        UNIQUE (question_id, concept_id)
);

CREATE INDEX idx_qc_question ON question_concepts (question_id);
CREATE INDEX idx_qc_concept  ON question_concepts (concept_id);
CREATE INDEX idx_qc_primary  ON question_concepts (concept_id, is_primary);


CREATE TABLE question_tags (
    question_id     VARCHAR(36)     NOT NULL,
    tag_id          VARCHAR(36)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (question_id, tag_id),

    CONSTRAINT fk_qt_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    CONSTRAINT fk_qt_tag
        FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_qt_tag ON question_tags (tag_id);


CREATE TABLE question_exam_types (
    question_id         VARCHAR(36)     NOT NULL,
    exam_type_id        VARCHAR(36)     NOT NULL,
    relevance_score     DECIMAL(3,2)    NOT NULL DEFAULT 1.00,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (question_id, exam_type_id),

    CONSTRAINT fk_qet_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    CONSTRAINT fk_qet_examtype
        FOREIGN KEY (exam_type_id) REFERENCES exam_types(id) ON DELETE CASCADE
);

CREATE INDEX idx_qet_examtype ON question_exam_types (exam_type_id);


CREATE TABLE question_versions (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    question_id         VARCHAR(36)     NOT NULL,
    version_number      INT             NOT NULL,
    question_text       TEXT            NOT NULL,
    options_snapshot    JSONB           NOT NULL,
    changed_by          VARCHAR(36)     NOT NULL,
    change_reason       VARCHAR(500),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_qv_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    CONSTRAINT uq_version
        UNIQUE (question_id, version_number)
);

CREATE INDEX idx_qv_question ON question_versions (question_id);


CREATE TABLE question_reports (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    question_id     VARCHAR(36)     NOT NULL,
    reported_by     VARCHAR(36)     NOT NULL,

    report_type     VARCHAR(30)     NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'OPEN',

    description     TEXT,
    resolved_by     VARCHAR(36),
    resolution_note TEXT,

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_qr_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    CONSTRAINT chk_report_type
        CHECK (report_type IN ('WRONG_ANSWER','TYPO','OUTDATED','AMBIGUOUS','DUPLICATE')),

    CONSTRAINT chk_report_status
        CHECK (status IN ('OPEN','UNDER_REVIEW','RESOLVED','DISMISSED'))
);

CREATE INDEX idx_qr_question ON question_reports (question_id);
CREATE INDEX idx_qr_status   ON question_reports (status);
CREATE INDEX idx_qr_reporter ON question_reports (reported_by);