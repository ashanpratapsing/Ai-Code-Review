-- Add advanced analysis columns to metrics table
ALTER TABLE metrics ADD COLUMN IF NOT EXISTS concurrency_analysis TEXT;
ALTER TABLE metrics ADD COLUMN IF NOT EXISTS collection_analysis TEXT;
ALTER TABLE metrics ADD COLUMN IF NOT EXISTS graph_analysis TEXT;
ALTER TABLE metrics ADD COLUMN IF NOT EXISTS runtime_analysis TEXT;
