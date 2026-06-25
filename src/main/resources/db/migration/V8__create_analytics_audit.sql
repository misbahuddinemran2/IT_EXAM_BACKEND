-- V8__create_analytics_audit.sql

CREATE TABLE question_analytics (
    id                          VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    question_id                 VARCHAR(36)     NOT NULL UNIQUE,
    total_attempts              BIGINT          NOT NULL DEFAULT 0,
    correct_attempts            BIGINT          NOT NULL DEFAULT 0,
    skip_count                  BIGINT          NOT NULL DEFAULT 0,
    avg_time_spent_sec          DECIMAL(8,2)    NOT NULL DEFAULT 0,
    accuracy_rate               DECIMAL(5,2)    NOT NULL DEFAULT 0,
    difficulty_score_actual     DECIMAL(3,2),
    discrimination_index        DECIMAL(3,2),
    last_computed_at            TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_qa_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE
);


CREATE TABLE audit_logs (
    id              BIGSERIAL       NOT NULL,

    actor_id        VARCHAR(36)     NOT NULL,
    actor_type      VARCHAR(20)     NOT NULL DEFAULT 'ADMIN',

    action          VARCHAR(100)    NOT NULL,
    resource_type   VARCHAR(50)     NOT NULL,
    resource_id     VARCHAR(36)     NOT NULL,

    old_value       JSONB,
    new_value       JSONB,

    ip_address      VARCHAR(45),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT chk_actor_type
        CHECK (actor_type IN ('ADMIN','SYSTEM','AI'))
);

CREATE INDEX idx_audit_actor
    ON audit_logs (actor_id);

CREATE INDEX idx_audit_resource
    ON audit_logs (resource_type, resource_id);

CREATE INDEX idx_audit_action
    ON audit_logs (action);

CREATE INDEX idx_audit_created
    ON audit_logs (created_at);


CREATE TABLE bulk_upload_jobs (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,

    uploaded_by     VARCHAR(36)     NOT NULL,
    file_name       VARCHAR(255)    NOT NULL,
    file_size_kb    INT             NOT NULL,

    total_rows      INT             NOT NULL DEFAULT 0,
    valid_rows      INT             NOT NULL DEFAULT 0,
    failed_rows     INT             NOT NULL DEFAULT 0,
    imported_rows   INT             NOT NULL DEFAULT 0,

    status          VARCHAR(30)     NOT NULL DEFAULT 'UPLOADED',

    error_report    JSONB,

    started_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT chk_bulk_status
        CHECK (
            status IN (
                'UPLOADED',
                'VALIDATING',
                'VALIDATION_DONE',
                'IMPORTING',
                'COMPLETED',
                'FAILED'
            )
        )
);

CREATE INDEX idx_bulk_uploader
    ON bulk_upload_jobs (uploaded_by);

CREATE INDEX idx_bulk_status
    ON bulk_upload_jobs (status);