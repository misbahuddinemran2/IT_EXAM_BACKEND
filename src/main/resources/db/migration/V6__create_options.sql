CREATE TABLE options (
    id              VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    question_id     VARCHAR(36)     NOT NULL,
    option_key      CHAR(1)         NOT NULL,
    option_text     TEXT            NOT NULL,
    option_text_bn  TEXT,
    is_correct      BOOLEAN         NOT NULL DEFAULT FALSE,
    explanation     TEXT,
    explanation_bn  TEXT,
    order_index     SMALLINT        NOT NULL DEFAULT 0,
    selection_count BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_options_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_option_key
        UNIQUE (question_id, option_key)
);

CREATE INDEX idx_options_question
    ON options (question_id);

CREATE INDEX idx_options_correct
    ON options (question_id, is_correct);