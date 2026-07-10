ALTER TABLE written_exam DROP CONSTRAINT IF EXISTS written_exam_education_level_check;
ALTER TABLE written_exam ALTER COLUMN education_level TYPE VARCHAR(20);
ALTER TABLE written_exam ADD CONSTRAINT written_exam_education_level_check
    CHECK (education_level IN ('CLASS_9','NEW_CLASS_10','SSC','HSC_1ST_YEAR','HSC_2ND_YEAR'));
