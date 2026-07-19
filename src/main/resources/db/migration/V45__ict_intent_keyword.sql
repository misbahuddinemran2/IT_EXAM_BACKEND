CREATE TABLE ict_intent_keyword (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    intent VARCHAR(50) NOT NULL CHECK (intent IN (
        'FEATURES', 'ADVANTAGE', 'DISADVANTAGE', 'APPLICATION',
        'EXAMPLE', 'COMPARISON', 'PROCESS', 'IMPORTANCE', 'DEFINITION',
        'CLASSIFICATION', 'FORMULA_CALCULATION', 'STRUCTURE',
        'FULL_FORM', 'SYNTAX_CODE', 'ADVANTAGE_DISADVANTAGE_COMBINED',
        'CONDITION_REQUIREMENT', 'RULE_LAW'
    )),
    keyword VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ict_intent_keyword_intent ON ict_intent_keyword(intent);
CREATE INDEX idx_ict_intent_keyword_active ON ict_intent_keyword(is_active);

CREATE TRIGGER trg_ict_intent_keyword_updated_at
BEFORE UPDATE ON ict_intent_keyword
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ডিফল্ট keyword entries
INSERT INTO ict_intent_keyword (intent, keyword) VALUES
('FEATURES', 'বৈশিষ্ট্য'), ('FEATURES', 'বৈশিষ্ট্যসমূহ'), ('FEATURES', 'গুণ'), ('FEATURES', 'ধর্ম'),
('ADVANTAGE', 'সুবিধা'), ('ADVANTAGE', 'উপকারিতা'), ('ADVANTAGE', 'লাভ'), ('ADVANTAGE', 'সুফল'),
('DISADVANTAGE', 'অসুবিধা'), ('DISADVANTAGE', 'ক্ষতি'), ('DISADVANTAGE', 'সমস্যা'), ('DISADVANTAGE', 'কুফল'),
('APPLICATION', 'ব্যবহার'), ('APPLICATION', 'প্রয়োগ'), ('APPLICATION', 'কোথায় ব্যবহৃত'), ('APPLICATION', 'ব্যবহৃত হয়'),
('EXAMPLE', 'উদাহরণ'), ('EXAMPLE', 'example'), ('EXAMPLE', 'উদাহরণসহ'),
('COMPARISON', 'পার্থক্য'), ('COMPARISON', 'তুলনা'), ('COMPARISON', 'differences'), ('COMPARISON', 'compare'),
('PROCESS', 'প্রক্রিয়া'), ('PROCESS', 'ধাপ'), ('PROCESS', 'কিভাবে'), ('PROCESS', 'কীভাবে'),
('IMPORTANCE', 'গুরুত্ব'), ('IMPORTANCE', 'প্রয়োজনীয়তা'), ('IMPORTANCE', 'কেন দরকার'),
('DEFINITION', 'কী'), ('DEFINITION', 'কি'), ('DEFINITION', 'সংজ্ঞা'), ('DEFINITION', 'বলতে কী বোঝায়'), ('DEFINITION', 'বুঝ'), ('DEFINITION', 'কাকে বলে'),
('CLASSIFICATION', 'প্রকারভেদ'), ('CLASSIFICATION', 'কত প্রকার'), ('CLASSIFICATION', 'ভাগ'), ('CLASSIFICATION', 'শ্রেণিবিভাগ'),
('FORMULA_CALCULATION', 'সূত্র'), ('FORMULA_CALCULATION', 'হিসাব করো'), ('FORMULA_CALCULATION', 'নির্ণয় করো'), ('FORMULA_CALCULATION', 'কনভার্ট করো'),
('STRUCTURE', 'গঠন'), ('STRUCTURE', 'অংশ'), ('STRUCTURE', 'উপাদান'), ('STRUCTURE', 'স্তর'),
('FULL_FORM', 'পূর্ণরূপ'), ('FULL_FORM', 'সংক্ষিপ্ত রূপ কী'), ('FULL_FORM', 'full form'),
('SYNTAX_CODE', 'সিনট্যাক্স'), ('SYNTAX_CODE', 'কোড লেখ'), ('SYNTAX_CODE', 'প্রোগ্রাম লেখ'),
('ADVANTAGE_DISADVANTAGE_COMBINED', 'সুবিধা অসুবিধা'), ('ADVANTAGE_DISADVANTAGE_COMBINED', 'ভালো মন্দ দিক'),
('CONDITION_REQUIREMENT', 'শর্ত'), ('CONDITION_REQUIREMENT', 'প্রয়োজনীয় উপাদান'), ('CONDITION_REQUIREMENT', 'কী কী লাগে'),
('RULE_LAW', 'নিয়ম'), ('RULE_LAW', 'সূত্র law'), ('RULE_LAW', 'নীতি');
