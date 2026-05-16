-- V9__Create_Analysis_History_Table.sql
-- Resolves Hibernate Schema Validation failures by syncing DB with Java Entities

-- 1. Create the missing analysis_history table
CREATE TABLE IF NOT EXISTS analysis_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    code_snippet TEXT,
    result_json TEXT,
    score INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analysis_history_user_id ON analysis_history(user_id);

-- 2. Add missing retry_count column to metrics table (used by Metrics.java)
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='metrics' AND column_name='retry_count') THEN
        ALTER TABLE metrics ADD COLUMN retry_count INTEGER DEFAULT 0;
    END IF;
END $$;
