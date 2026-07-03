ALTER TABLE exam_attempt_history
    ADD COLUMN total_questions INT NOT NULL DEFAULT 0,
    ADD COLUMN correct_count   INT NOT NULL DEFAULT 0,
    ADD COLUMN wrong_count     INT NOT NULL DEFAULT 0,
    ADD COLUMN skip_count      INT NOT NULL DEFAULT 0;
