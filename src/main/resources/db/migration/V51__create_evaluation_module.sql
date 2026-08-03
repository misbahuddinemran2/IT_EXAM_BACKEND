-- =========================================================
-- V51: Evaluation Module (Research/Journal Paper Infrastructure)
-- Purpose: Automatically evaluate the existing Citation-Aware
-- RAG pipeline (IctAskService) for research benchmarking.
-- This migration does NOT modify any existing table.
-- =========================================================

-- ---------------------------------------------------------
-- 1. EVALUATION DATASET
-- একটা প্রশ্ন-সেটকে domain ও language অনুযায়ী গ্রুপ করে
-- ---------------------------------------------------------
CREATE TABLE evaluation_dataset (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    name                VARCHAR(255)  NOT NULL,
    domain              VARCHAR(50)   NOT NULL DEFAULT 'HSC_ICT'
                             CHECK (domain IN (
                                 'HSC_ICT','SSC_ICT','BANK_IT','BCS_ICT',
                                 'MEDICAL','LAW','BENGALI_QA','ENGLISH_QA','OTHER'
                             )),
    language            VARCHAR(10)   NOT NULL DEFAULT 'bn',
    description         TEXT,
    created_by_admin_id VARCHAR(36),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_eval_dataset_admin FOREIGN KEY (created_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_eval_dataset_domain   ON evaluation_dataset (domain);
CREATE INDEX idx_eval_dataset_language ON evaluation_dataset (language);

DROP TRIGGER IF EXISTS trg_evaluation_dataset_updated_at ON evaluation_dataset;
CREATE TRIGGER trg_evaluation_dataset_updated_at
BEFORE UPDATE ON evaluation_dataset
FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------
-- 2. EVALUATION QUESTION
-- প্রতিটা গবেষণা প্রশ্ন + gold-standard reference answer +
-- human-evaluation/error-analysis সহায়ক মেটাডেটা
-- ---------------------------------------------------------
CREATE TABLE evaluation_question (
    id                     VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    dataset_id             VARCHAR(36)   NOT NULL,
    question_text          TEXT          NOT NULL,
    expected_answer         TEXT          NOT NULL,
    expected_writer_names    VARCHAR(500),

    difficulty              VARCHAR(20)   CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
    question_type           VARCHAR(30)   CHECK (question_type IN (
                                 'DEFINITION','FEATURES','ADVANTAGE','DISADVANTAGE',
                                 'APPLICATION','EXAMPLE','COMPARISON','PROCESS',
                                 'IMPORTANCE','CLASSIFICATION','FORMULA_CALCULATION',
                                 'STRUCTURE','FULL_FORM','SYNTAX_CODE','OTHER'
                             )),
    reference_book           VARCHAR(255),
    reference_page            INT,
    reference_chunk_id         VARCHAR(36),

    subject_id             VARCHAR(36),
    chapter_id             VARCHAR(36),
    topic_id               VARCHAR(36),
    is_active               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_eval_question_dataset FOREIGN KEY (dataset_id) REFERENCES evaluation_dataset(id),
    CONSTRAINT fk_eval_question_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_eval_question_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_eval_question_topic   FOREIGN KEY (topic_id)   REFERENCES topics(id),
    CONSTRAINT fk_eval_question_chunk   FOREIGN KEY (reference_chunk_id) REFERENCES ict_book_chunk(id)
);

CREATE INDEX idx_eval_question_dataset    ON evaluation_question (dataset_id);
CREATE INDEX idx_eval_question_active     ON evaluation_question (is_active);
CREATE INDEX idx_eval_question_difficulty ON evaluation_question (difficulty);
CREATE INDEX idx_eval_question_type       ON evaluation_question (question_type);

DROP TRIGGER IF EXISTS trg_evaluation_question_updated_at ON evaluation_question;
CREATE TRIGGER trg_evaluation_question_updated_at
BEFORE UPDATE ON evaluation_question
FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------
-- 3. EVALUATION PROFILE  (পূর্বে evaluation_configuration)
-- একটা reusable experiment profile
-- (Gemini Flash Default / Gemini High Accuracy / Claude Research ইত্যাদি)
-- prompt_version এখানে রাখা হয়নি — সেটা এখন evaluation_prompt
-- টেবিলের দায়িত্ব (redundancy এড়াতে)
-- ---------------------------------------------------------
CREATE TABLE evaluation_profile (
    id                    VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    name                  VARCHAR(150)    NOT NULL,
    model_name            VARCHAR(100)    NOT NULL,
    embedding_model       VARCHAR(100),
    top_k                 INT             NOT NULL DEFAULT 5,
    similarity_threshold  DECIMAL(5,4),
    temperature           DECIMAL(3,2),
    chunk_strategy        VARCHAR(100),
    max_tokens            INT,
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_eval_profile_model ON evaluation_profile (model_name);

DROP TRIGGER IF EXISTS trg_evaluation_profile_updated_at ON evaluation_profile;
CREATE TRIGGER trg_evaluation_profile_updated_at
BEFORE UPDATE ON evaluation_profile
FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------
-- 4. EVALUATION PROMPT
-- প্রতিটা experiment-এ ঠিক কোন prompt (system+user, version)
-- ব্যবহার হয়েছিল তার স্থায়ী রেজিস্ট্রি
-- ---------------------------------------------------------
CREATE TABLE evaluation_prompt (
    id             VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    name           VARCHAR(150)  NOT NULL,
    version        VARCHAR(50)   NOT NULL,
    system_prompt  TEXT,
    user_prompt    TEXT          NOT NULL,
    notes          TEXT,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_eval_prompt_name_version UNIQUE (name, version)
);

DROP TRIGGER IF EXISTS trg_evaluation_prompt_updated_at ON evaluation_prompt;
CREATE TRIGGER trg_evaluation_prompt_updated_at
BEFORE UPDATE ON evaluation_prompt
FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------
-- 5. EVALUATION RUN
-- একটা নির্দিষ্ট experiment execution
-- configuration_snapshot = পুরো experiment settings-এর immutable
-- snapshot, dataset/profile/prompt পরে বদলে গেলেও এই run
-- হুবহু reproduce করা যাবে
-- ---------------------------------------------------------
CREATE TABLE evaluation_run (
    id                     VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    dataset_id             VARCHAR(36)   NOT NULL,
    dataset_version         VARCHAR(50),
    profile_id             VARCHAR(36)   NOT NULL,
    prompt_id              VARCHAR(36)   NOT NULL,
    configuration_snapshot   JSONB         NOT NULL,

    status                VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                               CHECK (status IN ('PENDING','RUNNING','COMPLETED','FAILED')),
    total_questions       INT           NOT NULL DEFAULT 0,
    processed_questions   INT           NOT NULL DEFAULT 0,
    started_at            TIMESTAMP,
    completed_at          TIMESTAMP,
    triggered_by_admin_id VARCHAR(36),
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_eval_run_dataset FOREIGN KEY (dataset_id) REFERENCES evaluation_dataset(id),
    CONSTRAINT fk_eval_run_profile FOREIGN KEY (profile_id) REFERENCES evaluation_profile(id),
    CONSTRAINT fk_eval_run_prompt  FOREIGN KEY (prompt_id)  REFERENCES evaluation_prompt(id),
    CONSTRAINT fk_eval_run_admin   FOREIGN KEY (triggered_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_eval_run_dataset ON evaluation_run (dataset_id);
CREATE INDEX idx_eval_run_profile ON evaluation_run (profile_id);
CREATE INDEX idx_eval_run_prompt  ON evaluation_run (prompt_id);
CREATE INDEX idx_eval_run_status  ON evaluation_run (status);

-- JSONB snapshot ভবিষ্যতে key-based query/filter করতে হলে
CREATE INDEX idx_eval_run_configuration_snapshot_gin
    ON evaluation_run USING GIN (configuration_snapshot);

DROP TRIGGER IF EXISTS trg_evaluation_run_updated_at ON evaluation_run;
CREATE TRIGGER trg_evaluation_run_updated_at
BEFORE UPDATE ON evaluation_run
FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------
-- 6. EVALUATION RESULT
-- প্রতিটা run-এ প্রতিটা প্রশ্নের output + সব raw metric value।
-- model_name/prompt_version এখানে ইচ্ছাকৃতভাবে denormalized রাখা
-- হয়েছে — join ছাড়াই high-volume reporting/model-comparison
-- query দ্রুত চালানোর জন্য।
-- ---------------------------------------------------------
CREATE TABLE evaluation_result (
    id                        VARCHAR(36)      NOT NULL DEFAULT gen_random_uuid()::text,
    run_id                    VARCHAR(36)      NOT NULL,
    question_id               VARCHAR(36)      NOT NULL,

    -- IctAskService.ask() থেকে reuse করা raw output
    generated_answer           TEXT,
    response_path              VARCHAR(20),
    matched_writer_names        TEXT,
    answer_found                BOOLEAN          NOT NULL DEFAULT FALSE,
    from_cache                  BOOLEAN          NOT NULL DEFAULT FALSE,

    -- Retrieval metadata (IctBookChunkRepository থেকে আলাদাভাবে capture করা)
    retrieved_chunk_ids          JSONB,
    retrieved_chunk_distances     JSONB,
    closest_chunk_distance        DOUBLE PRECISION,
    retrieved_chunk_count          INT,
    candidate_chunk_count           INT,

    -- Performance metric (retrieval vs generation আলাদা করে ভাঙা)
    retrieval_latency_ms             INT,
    llm_latency_ms                    INT,
    response_time_ms                   INT,

    -- Reporting-এর জন্য denormalized snapshot fields
    prompt_version                      VARCHAR(50),
    model_name                           VARCHAR(100),
    token_input                           INT,
    token_output                           INT,

    -- Answer-quality metric (post-processing এ হিসাব করে আপডেট হবে)
    exact_match                           BOOLEAN,
    semantic_similarity_score              DECIMAL(5,4),
    token_f1_score                          DECIMAL(5,4),

    -- Citation metric
    citation_coverage                       DECIMAL(5,4),
    citation_precision                       DECIMAL(5,4),
    citation_recall                           DECIMAL(5,4),
    citation_faithfulness                      DECIMAL(5,4),

    status                      VARCHAR(20)      NOT NULL DEFAULT 'PENDING'
                                     CHECK (status IN ('PENDING','SUCCESS','FAILED')),
    error_message                TEXT,

    created_at                   TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_eval_result_run      FOREIGN KEY (run_id)      REFERENCES evaluation_run(id),
    CONSTRAINT fk_eval_result_question FOREIGN KEY (question_id) REFERENCES evaluation_question(id),
    CONSTRAINT uk_eval_result_run_question UNIQUE (run_id, question_id)
);

CREATE INDEX idx_eval_result_run         ON evaluation_result (run_id);
CREATE INDEX idx_eval_result_question    ON evaluation_result (question_id);
CREATE INDEX idx_eval_result_status      ON evaluation_result (status);
CREATE INDEX idx_eval_result_model_name  ON evaluation_result (model_name);

-- উচ্চ-volume reporting-এর জন্য কম্পোজিট index
CREATE INDEX idx_eval_result_run_status ON evaluation_result (run_id, status);

-- JSONB retrieved_chunk_ids ভবিষ্যতে key/containment query করতে হলে
CREATE INDEX idx_eval_result_retrieved_chunk_ids_gin
    ON evaluation_result USING GIN (retrieved_chunk_ids);

DROP TRIGGER IF EXISTS trg_evaluation_result_updated_at ON evaluation_result;
CREATE TRIGGER trg_evaluation_result_updated_at
BEFORE UPDATE ON evaluation_result
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
