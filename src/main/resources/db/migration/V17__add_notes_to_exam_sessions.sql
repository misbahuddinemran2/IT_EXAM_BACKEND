-- V17__add_notes_to_exam_sessions.sql

ALTER TABLE user_exam_sessions
    ADD COLUMN notes TEXT NULL;