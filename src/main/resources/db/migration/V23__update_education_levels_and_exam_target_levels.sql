-- ============================================
-- V23: Education level expansion + exam target levels
-- ============================================

-- 1. education_level কলামের length বাড়ানো (VARCHAR(10) -> VARCHAR(20))
ALTER TABLE users 
ALTER COLUMN education_level TYPE VARCHAR(20);

-- 2. পুরনো check constraint বাদ দেওয়া
ALTER TABLE users 
DROP CONSTRAINT IF EXISTS users_education_level_check;

-- 3. পুরনো ডাটা migrate করা (HSC -> HSC_1ST_YEAR)
UPDATE users 
SET education_level = 'HSC_1ST_YEAR' 
WHERE education_level = 'HSC';

-- 4. নতুন enum লিস্ট দিয়ে constraint যোগ করা
ALTER TABLE users 
ADD CONSTRAINT users_education_level_check 
CHECK (education_level IN (
    'CLASS_9', 'NEW_CLASS_10', 'SSC', 
    'HSC_1ST_YEAR', 'HSC_2ND_YEAR', 
    'HONORS', 'MASTERS', 'OTHER'
));

-- 5. exams টেবিলে target_levels কলাম যোগ করা (multi-class targeting)
ALTER TABLE exams 
ADD COLUMN IF NOT EXISTS target_levels JSONB NOT NULL DEFAULT '["ALL"]';
