CREATE TABLE live_exam_sessions (
    id                  VARCHAR(36)   NOT NULL,
    exam_id             VARCHAR(36)   NOT NULL,
    user_id             VARCHAR(36)   NOT NULL,

    status              VARCHAR(20)   NOT NULL DEFAULT 'IN_PROGRESS'
                            CHECK (status IN ('IN_PROGRESS','DISCONNECTED','SUBMITTED','AUTO_SUBMITTED')),

    started_at          TIMESTAMP     NOT NULL,
    expires_at          TIMESTAMP     NOT NULL,      -- started_at + duration_minutes
    last_seen_at        TIMESTAMP     NOT NULL,       -- heartbeat / last activity
    disconnected_at     TIMESTAMP,                    -- set when grace period starts
    submitted_at        TIMESTAMP,

    answers             JSONB         NOT NULL DEFAULT '{}',   -- {questionId: optionId}
    marked_for_review   JSONB         NOT NULL DEFAULT '[]',   -- [questionId,...]

    obtained_marks      DECIMAL(8,2)  NOT NULL DEFAULT 0.00,
    total_marks         DECIMAL(8,2)  NOT NULL DEFAULT 0.00,

    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_live_exam_user_exam UNIQUE (exam_id, user_id)  -- 1 attempt only
);

CREATE INDEX idx_live_session_user ON live_exam_sessions(user_id);
CREATE INDEX idx_live_session_exam ON live_exam_sessions(exam_id);
CREATE INDEX idx_live_session_status ON live_exam_sessions(status);

DROP TRIGGER IF EXISTS trg_live_exam_sessions_updated_at ON live_exam_sessions;
CREATE TRIGGER trg_live_exam_sessions_updated_at
BEFORE UPDATE ON live_exam_sessions
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
