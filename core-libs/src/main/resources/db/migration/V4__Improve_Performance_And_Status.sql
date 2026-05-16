-- Add status and failure_reason to metrics
ALTER TABLE metrics ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
ALTER TABLE metrics ADD COLUMN failure_reason TEXT;

-- Add indexes for common queries
CREATE INDEX idx_metrics_code_file_id ON metrics(code_file_id);
CREATE INDEX idx_code_files_project_id ON code_files(project_id);
CREATE INDEX idx_projects_user_id ON projects(user_id);

-- Update existing records to COMPLETED if they have content
UPDATE metrics SET status = 'COMPLETED' WHERE summary IS NOT NULL;
