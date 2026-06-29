-- ============================================================
-- V20: Onboarding Slides (PostgreSQL / Neon)
-- ============================================================

CREATE TABLE IF NOT EXISTS onboarding_slides (
    id            VARCHAR(36)  PRIMARY KEY DEFAULT gen_random_uuid()::text,
    title         VARCHAR(255) NOT NULL,
    subtitle      VARCHAR(255),
    description   TEXT,
    image_url     VARCHAR(500),
    animation_url VARCHAR(500),
    slide_order   INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO onboarding_slides (id, title, subtitle, description, image_url, slide_order, is_active) VALUES
(
    gen_random_uuid()::text,
    'Practice Smart',
    'বুদ্ধিমত্তার সাথে প্র্যাকটিস করুন',
    'BCS, Bank IT, NTRCA সহ সকল সরকারি IT পরীক্ষার জন্য প্রস্তুত হন।',
    '',
    1,
    TRUE
),
(
    gen_random_uuid()::text,
    'Track Progress',
    'আপনার অগ্রগতি ট্র্যাক করুন',
    'বিস্তারিত বিশ্লেষণ ও লিডারবোর্ড দিয়ে নিজেকে মূল্যায়ন করুন।',
    '',
    2,
    TRUE
),
(
    gen_random_uuid()::text,
    'Compete & Win',
    'প্রতিযোগিতা করুন ও জিতুন',
    'লাইভ এক্সামে অংশ নিন এবং সেরা পরীক্ষার্থী হিসেবে নিজেকে প্রমাণ করুন।',
    '',
    3,
    TRUE
);