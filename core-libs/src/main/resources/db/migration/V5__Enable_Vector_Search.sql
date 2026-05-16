-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding column to metrics (1536 is standard for OpenAI/similar, adjust if needed)
ALTER TABLE metrics ADD COLUMN embedding vector(1536);

-- Add index for cosine similarity search
CREATE INDEX idx_metrics_embedding ON metrics USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
