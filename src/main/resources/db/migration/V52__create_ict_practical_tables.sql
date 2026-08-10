-- ============================================
-- ICT PRACTICAL MODULE
-- ============================================

CREATE TABLE practical_chapters (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,          -- HTML / Programming / Database
    name_bn VARCHAR(100),
    icon VARCHAR(50),
    order_number INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE practical_experiments (
    id VARCHAR(36) PRIMARY KEY,
    chapter_id VARCHAR(36) NOT NULL REFERENCES practical_chapters(id) ON DELETE CASCADE,

    title VARCHAR(255) NOT NULL,
    title_bn VARCHAR(255),
    description TEXT,

    is_active BOOLEAN NOT NULL DEFAULT FALSE,   -- admin toggle, student শুধু active দেখবে
    target_sessions JSONB NOT NULL DEFAULT '["ALL"]',  -- যেমন ["2025-2026"]

    order_number INT NOT NULL DEFAULT 0,

    created_by VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_practical_experiments_chapter ON practical_experiments(chapter_id);

CREATE TABLE practical_khata (
    id VARCHAR(36) PRIMARY KEY,
    experiment_id VARCHAR(36) NOT NULL UNIQUE REFERENCES practical_experiments(id) ON DELETE CASCADE,

    khata_type VARCHAR(10) NOT NULL DEFAULT 'TEXT',  -- PDF / TEXT / BOTH
    pdf_url TEXT,
    text_content TEXT,

    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE practical_viva_questions (
    id VARCHAR(36) PRIMARY KEY,
    experiment_id VARCHAR(36) NOT NULL REFERENCES practical_experiments(id) ON DELETE CASCADE,

    question TEXT NOT NULL,
    question_bn TEXT,
    answer TEXT,       -- admin optional দিতে পারে
    order_number INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP
);

CREATE INDEX idx_practical_viva_experiment ON practical_viva_questions(experiment_id);

-- ৩টা fixed chapter সিড করা (admin পরে নাম/আইকন এডিট করতে পারবে)
INSERT INTO practical_chapters (id, name, name_bn, icon, order_number, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 'HTML', 'এইচটিএমএল', 'code-slash-outline', 1, NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'Programming', 'প্রোগ্রামিং', 'terminal-outline', 2, NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'Database', 'ডাটাবেস', 'server-outline', 3, NOW(), NOW());
