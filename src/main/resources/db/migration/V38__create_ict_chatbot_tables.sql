-- =========================================
-- V38: ICT Chatbot Module
-- OCR Upload, Book Chunk, Answer Cache
-- =========================================

CREATE EXTENSION IF NOT EXISTS vector;

-- 1. OCR UPLOAD (admin ingestion staging table)
CREATE TABLE ict_ocr_upload (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    ocr_text            TEXT          NOT NULL,
    writer_name         VARCHAR(255),
    subject_id          VARCHAR(36),
    chapter_id          VARCHAR(36),
    topic_id            VARCHAR(36),
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING','REVIEWED','VECTORIZED')),
    reviewed_by_admin_id VARCHAR(36),
    reviewed_at         TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ict_ocr_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_ict_ocr_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_ict_ocr_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_ict_ocr_admin FOREIGN KEY (reviewed_by_admin_id) REFERENCES admin_users(id)
);

CREATE INDEX idx_ict_ocr_status ON ict_ocr_upload (status);

-- 2. BOOK CHUNK (final vectorized knowledge base)
CREATE TABLE ict_book_chunk (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    source_upload_id    VARCHAR(36),
    content             TEXT          NOT NULL,
    writer_name         VARCHAR(255)  NOT NULL,
    subject_id          VARCHAR(36),
    chapter_id          VARCHAR(36),
    topic_id            VARCHAR(36),
    embedding           vector(768),
    diagram_url         VARCHAR(500),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ict_chunk_upload FOREIGN KEY (source_upload_id) REFERENCES ict_ocr_upload(id),
    CONSTRAINT fk_ict_chunk_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_ict_chunk_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT fk_ict_chunk_topic FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE INDEX idx_ict_chunk_subject ON ict_book_chunk (subject_id);
CREATE INDEX ict_chunk_embedding_idx ON ict_book_chunk
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 3. ANSWER CACHE
CREATE TABLE ict_answer_cache (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    question_text       TEXT          NOT NULL,
    question_embedding  vector(768)   NOT NULL,
    cached_answer       TEXT          NOT NULL,
    hit_count           INT           NOT NULL DEFAULT 1,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX ict_cache_embedding_idx ON ict_answer_cache
    USING ivfflat (question_embedding vector_cosine_ops) WITH (lists = 100);

-- 4. updated_at auto triggers (reusing existing set_updated_at() function)
DROP TRIGGER IF EXISTS trg_ict_ocr_upload_updated_at ON ict_ocr_upload;
CREATE TRIGGER trg_ict_ocr_upload_updated_at
BEFORE UPDATE ON ict_ocr_upload
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_ict_book_chunk_updated_at ON ict_book_chunk;
CREATE TRIGGER trg_ict_book_chunk_updated_at
BEFORE UPDATE ON ict_book_chunk
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_ict_answer_cache_updated_at ON ict_answer_cache;
CREATE TRIGGER trg_ict_answer_cache_updated_at
BEFORE UPDATE ON ict_answer_cache
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
