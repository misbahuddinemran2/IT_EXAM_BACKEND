-- =========================================
-- V40: ICT Chatbot Quick Reply
-- Greeting/casual প্রশ্নের জন্য hardcoded উত্তর
-- (Gemini/embedding API call এড়াতে)
-- =========================================

CREATE TABLE ict_quick_reply (
    id                  VARCHAR(36)   NOT NULL DEFAULT gen_random_uuid()::text,
    keywords            TEXT          NOT NULL,   -- comma-separated: "কেমন আছ,কি অবস্থা,হ্যালো"
    reply_text          TEXT          NOT NULL,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_ict_quick_reply_active ON ict_quick_reply (is_active);

DROP TRIGGER IF EXISTS trg_ict_quick_reply_updated_at ON ict_quick_reply;
CREATE TRIGGER trg_ict_quick_reply_updated_at
BEFORE UPDATE ON ict_quick_reply
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- কিছু default entry (আপনি admin app থেকে পরে আরও যোগ/এডিট করতে পারবেন)
INSERT INTO ict_quick_reply (keywords, reply_text) VALUES
('কেমন আছ,কি অবস্থা,ভালো আছ', 'আমি ভালো আছি! তুমি ICT বই থেকে কী জানতে চাও বলো তো? 📘'),
('তুমি কে,তোমার নাম কি,তুমি কি', 'আমি তোমার ICT AI Tutor — HSC ICT বইয়ের যেকোনো প্রশ্নের উত্তর খুঁজে দিতে পারি।'),
('হ্যালো,হাই,আসসালামু আলাইকুম', 'হ্যালো! ICT বই থেকে কিছু জানতে চাইলে জিজ্ঞেস করো।'),
('ধন্যবাদ,থ্যাংক ইউ,থ্যাঙ্কস', 'তোমাকেও ধন্যবাদ! আরও কিছু জানার থাকলে বলো।');
