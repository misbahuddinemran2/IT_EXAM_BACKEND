-- V26__add_cycle_number.sql

ALTER TABLE exams ADD COLUMN cycle_number INT NOT NULL DEFAULT 1;

ALTER TABLE live_exam_sessions ADD COLUMN cycle_number INT NOT NULL DEFAULT 1;

ALTER TABLE live_exam_sessions DROP CONSTRAINT uk_live_exam_user_exam;
ALTER TABLE live_exam_sessions ADD CONSTRAINT uk_live_exam_user_exam_cycle
    UNIQUE (exam_id, user_id, cycle_number);

CREATE INDEX idx_live_session_user_exam ON live_exam_sessions(exam_id, user_id);
