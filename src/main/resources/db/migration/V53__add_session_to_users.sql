DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'session'
    ) THEN
        ALTER TABLE users ADD COLUMN session VARCHAR(20);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_users_session ON users (session);
