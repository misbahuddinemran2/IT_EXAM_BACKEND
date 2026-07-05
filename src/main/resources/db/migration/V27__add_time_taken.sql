-- V27__add_time_taken.sql

ALTER TABLE exam_attempt_history ADD COLUMN time_taken_seconds INT NOT NULL DEFAULT 0;
