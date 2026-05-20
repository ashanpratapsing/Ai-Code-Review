-- Code execution persistence (per-user)
CREATE TABLE code_executions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_file_id BIGINT REFERENCES code_files(id) ON DELETE SET NULL,
    language VARCHAR(50) NOT NULL,
    source_code TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    compile_error TEXT,
    memory_limit_mb INT DEFAULT 128,
    timeout_ms INT DEFAULT 5000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE execution_test_results (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL REFERENCES code_executions(id) ON DELETE CASCADE,
    case_order INT NOT NULL DEFAULT 0,
    stdin TEXT,
    expected_output TEXT,
    actual_output TEXT,
    stderr TEXT,
    status VARCHAR(50),
    error_message TEXT,
    execution_time_ms BIGINT DEFAULT 0
);

CREATE TABLE execution_logs (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL REFERENCES code_executions(id) ON DELETE CASCADE,
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Enrich analysis history with ownership links
ALTER TABLE analysis_history
    ADD COLUMN IF NOT EXISTS project_id BIGINT REFERENCES projects(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS code_file_id BIGINT REFERENCES code_files(id) ON DELETE SET NULL;

-- Default role for existing users
UPDATE users SET role = 'USER' WHERE role IS NULL;

CREATE INDEX IF NOT EXISTS idx_projects_user_id ON projects(user_id);
CREATE INDEX IF NOT EXISTS idx_code_files_project_id ON code_files(project_id);
CREATE INDEX IF NOT EXISTS idx_code_executions_user_id ON code_executions(user_id);
CREATE INDEX IF NOT EXISTS idx_code_executions_created_at ON code_executions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_execution_test_results_execution_id ON execution_test_results(execution_id);
CREATE INDEX IF NOT EXISTS idx_analysis_history_user_created ON analysis_history(user_id, created_at DESC);
