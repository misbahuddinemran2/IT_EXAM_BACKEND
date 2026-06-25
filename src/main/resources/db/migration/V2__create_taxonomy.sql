-- V2__create_taxonomy.sql

CREATE TABLE subjects (
id            VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
name          VARCHAR(100)    NOT NULL,
name_bn       VARCHAR(100),
code          VARCHAR(20)     NOT NULL UNIQUE,
is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (id)
);

CREATE INDEX idx_subjects_code
ON subjects (code);

CREATE INDEX idx_subjects_active
ON subjects (is_active);

CREATE TABLE chapters (
id            VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
subject_id    VARCHAR(36)     NOT NULL,
name          VARCHAR(200)    NOT NULL,
name_bn       VARCHAR(200),
order_index   INT             NOT NULL DEFAULT 0,
is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (id),

CONSTRAINT fk_chapters_subject
    FOREIGN KEY (subject_id)
    REFERENCES subjects(id)
    ON DELETE CASCADE

);

CREATE INDEX idx_chapters_subject
ON chapters (subject_id);

CREATE INDEX idx_chapters_active
ON chapters (is_active);

CREATE TABLE topics (
id            VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
chapter_id    VARCHAR(36)     NOT NULL,
name          VARCHAR(200)    NOT NULL,
name_bn       VARCHAR(200),
description   TEXT,
order_index   INT             NOT NULL DEFAULT 0,
is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (id),

CONSTRAINT fk_topics_chapter
    FOREIGN KEY (chapter_id)
    REFERENCES chapters(id)
    ON DELETE CASCADE

);

CREATE INDEX idx_topics_chapter
ON topics (chapter_id);

CREATE INDEX idx_topics_active
ON topics (is_active);