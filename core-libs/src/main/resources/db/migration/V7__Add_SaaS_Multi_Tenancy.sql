-- Create organizations table
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stripe_customer_id VARCHAR(255),
    plan_tier VARCHAR(50) DEFAULT 'FREE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create organization_users table (RBAC)
CREATE TABLE organization_users (
    org_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'MEMBER',
    PRIMARY KEY (org_id, user_id)
);

-- Alter existing tables to add organization_id
ALTER TABLE projects ADD COLUMN organization_id UUID;
ALTER TABLE projects ADD CONSTRAINT fk_project_org FOREIGN KEY (organization_id) REFERENCES organizations(id);

ALTER TABLE code_files ADD COLUMN organization_id UUID;
ALTER TABLE code_files ADD CONSTRAINT fk_codefile_org FOREIGN KEY (organization_id) REFERENCES organizations(id);

-- Create an index for performance
CREATE INDEX idx_projects_org_id ON projects(organization_id);
CREATE INDEX idx_code_files_org_id ON code_files(organization_id);
