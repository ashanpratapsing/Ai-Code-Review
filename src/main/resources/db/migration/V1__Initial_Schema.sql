CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL, -- Renamed from 'name' to match entity
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT REFERENCES users(id)
);

CREATE TABLE code_files (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    code_content TEXT,
    language VARCHAR(50),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    project_id BIGINT REFERENCES projects(id)
);

CREATE TABLE ai_reports (
    id BIGSERIAL PRIMARY KEY,
    bugs TEXT,               -- Added missing field
    optimization TEXT,       -- Added missing field
    time_complexity TEXT,    -- Added missing field
    code_smells TEXT,        -- Added missing field
    refactored_code TEXT,    -- Added missing field
    unit_tests TEXT,         -- Added missing field
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    code_file_id BIGINT REFERENCES code_files(id)
);

CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    lines_of_code INT,
    number_of_functions INT,
    number_of_loops INT,
    nested_loops INT,
    complexity_score INT,
    code_file_id BIGINT REFERENCES code_files(id)
);
