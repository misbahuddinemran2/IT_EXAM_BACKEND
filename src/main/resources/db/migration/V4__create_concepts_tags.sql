-- V4__create_concepts_tags.sql

CREATE TABLE concepts (
    id                  VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    topic_id            VARCHAR(36)     NOT NULL,
    parent_concept_id   VARCHAR(36),
    name                VARCHAR(300)    NOT NULL,
    name_bn             VARCHAR(300),
    description         TEXT,
    concept_type        VARCHAR(15)     NOT NULL DEFAULT 'DEFINITION'
                            CHECK (concept_type IN (
                                'DEFINITION','PROCESS','FORMULA','PRINCIPLE','FACT'
                            )),
    difficulty_level    SMALLINT        NOT NULL DEFAULT 3,
    importance_score    DECIMAL(3,2)    NOT NULL DEFAULT 0.50,
    embedding_vector    TEXT,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(36)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_concepts_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_concepts_parent
        FOREIGN KEY (parent_concept_id)
        REFERENCES concepts(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_concepts_topic  ON concepts (topic_id);
CREATE INDEX idx_concepts_parent ON concepts (parent_concept_id);
CREATE INDEX idx_concepts_active ON concepts (is_active);


CREATE TABLE tags (
    id          VARCHAR(36)     NOT NULL DEFAULT gen_random_uuid()::text,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    tag_type    VARCHAR(15)     NOT NULL DEFAULT 'CUSTOM'
                    CHECK (tag_type IN (
                        'SUBJECT','EXAM_TYPE','DIFFICULTY','TOPIC','CUSTOM'
                    )),
    color_code  VARCHAR(7)              DEFAULT '#6366f1',
    usage_count INT             NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_tags_name ON tags (name);
CREATE INDEX idx_tags_type ON tags (tag_type);