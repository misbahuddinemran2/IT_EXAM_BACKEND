CREATE TABLE ict_query_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100),
    question TEXT NOT NULL,
    response_path VARCHAR(20) NOT NULL,
    answer_found BOOLEAN NOT NULL DEFAULT FALSE,
    matched_writer_names TEXT,
    closest_chunk_distance DOUBLE PRECISION,
    response_time_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_query_log_created_at ON ict_query_log (created_at);
CREATE INDEX idx_query_log_response_path ON ict_query_log (response_path);


CREATE TABLE ict_query_summary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    question TEXT NOT NULL,
    ask_count INT NOT NULL DEFAULT 0,
    not_found_count INT NOT NULL DEFAULT 0,
    quick_reply_count INT NOT NULL DEFAULT 0,
    cache_hit_count INT NOT NULL DEFAULT 0,
    gemini_generated_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_query_summary_gemini_count ON ict_query_summary (gemini_generated_count DESC);
CREATE INDEX idx_query_summary_not_found_count ON ict_query_summary (not_found_count DESC);
