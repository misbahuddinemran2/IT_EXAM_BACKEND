-- ICT Answer Rewrite Cache Table
CREATE TABLE ict_rewrite_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_answer_hash VARCHAR(64) NOT NULL,
    category VARCHAR(30) NOT NULL,
    rewritten_answer TEXT NOT NULL,
    hit_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_rewrite_cache UNIQUE (original_answer_hash, category)
);

CREATE INDEX idx_rewrite_cache_lookup
    ON ict_rewrite_cache (original_answer_hash, category);


-- ICT Rewrite Keyword Table (admin editable, quick-reply এর মতো pattern)
CREATE TABLE ict_rewrite_keyword (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(30) NOT NULL,
    keywords TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Default keyword entries (৮টা ক্যাটাগরির জন্য)
INSERT INTO ict_rewrite_keyword (category, keywords, is_active) VALUES
('SHORTEN', 'ছোট করে দাও,সংক্ষিপ্ত করো,সংক্ষেপে বলো,কমাও,আরো ছোট,একটু ছোট করো,shorten,short করে দাও', TRUE),
('EXPAND', 'বড় করে লিখো,বিস্তারিত লিখো,আরো বিস্তারিত,detail এ বলো,আরো লিখো,expand করো,আরো বড়', TRUE),
('EXAM_FORMAT', 'পরীক্ষার মতো লিখো,পরীক্ষার মত করে দাও,board exam format,লিখিত পরীক্ষার উত্তর,exam format এ দাও,written answer format', TRUE),
('SIMPLIFY', 'সহজ ভাষায় বলো,আরো সহজ করে,সহজ করে বুঝাও,কঠিন লাগছে,simple করে বলো,easy করে বলো', TRUE),
('BULLET_POINTS', 'পয়েন্ট করে দাও,পয়েন্ট আকারে,লিস্ট আকারে বলো,ধাপে ধাপে দাও,bullet points,point form এ দাও', TRUE),
('IMPROVE', 'আরো ভালো করে লিখো,ভালো করে লিখো,উন্নত করো,ঠিক বুঝি নাই,আবার লিখো,improve করো', TRUE),
('WITH_EXAMPLE', 'উদাহরণ দাও,উদাহরণ সহ বুঝাও,example সহ লিখো,বাস্তব উদাহরণ,example দাও,উদাহরণ সহ', TRUE),
('COMPARE_FORMAT', 'তুলনা করে দাও,পার্থক্য বলো,compare করে লিখো,তুলনামূলক ভাবে দাও,টেবিল আকারে পার্থক্য,difference বলো', TRUE);
