-- V3__create_exam_types.sql

CREATE TABLE exam_types (
id                VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
name              VARCHAR(100)    NOT NULL,
name_bn           VARCHAR(100),
code              VARCHAR(30)     NOT NULL UNIQUE,
description       TEXT,
conducting_body   VARCHAR(200),
is_active         BOOLEAN         NOT NULL DEFAULT TRUE,
created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (id)

);

CREATE INDEX idx_exam_types_code
ON exam_types (code);

CREATE INDEX idx_exam_types_active
ON exam_types (is_active);

-- Seed Data

INSERT INTO exam_types (
id,
name,
name_bn,
code,
conducting_body
)
VALUES
(
gen_random_uuid()::text,
'BCS ICT',
'বিসিএস আইসিটি',
'BCS_ICT',
'BPSC'
),
(
gen_random_uuid()::text,
'NTRCA ICT',
'এনটিআরসিএ আইসিটি',
'NTRCA_ICT',
'NTRCA'
),
(
gen_random_uuid()::text,
'Bank IT Officer',
'ব্যাংক আইটি অফিসার',
'BANK_IT',
'Bangladesh Bank'
),
(
gen_random_uuid()::text,
'Govt IT Job',
'সরকারি আইটি চাকরি',
'GOVT_IT',
'Government'
);