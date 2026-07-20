ALTER TABLE ict_query_log
    ADD COLUMN quick_reply_match_type VARCHAR(20),
    ADD COLUMN quick_reply_match_score DOUBLE PRECISION,
    ADD COLUMN quick_reply_matched_keyword TEXT;

CREATE INDEX idx_ict_query_log_match_type ON ict_query_log(quick_reply_match_type);
