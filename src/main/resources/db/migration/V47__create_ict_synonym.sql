CREATE TABLE ict_synonym (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    word VARCHAR(150) NOT NULL,
    canonical_word VARCHAR(150) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_ict_synonym_word ON ict_synonym(word);
CREATE INDEX idx_ict_synonym_canonical ON ict_synonym(canonical_word);
