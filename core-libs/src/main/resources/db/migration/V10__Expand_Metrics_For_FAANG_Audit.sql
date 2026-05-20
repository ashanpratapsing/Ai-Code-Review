ALTER TABLE metrics ADD COLUMN security_issues TEXT;
ALTER TABLE metrics ADD COLUMN suggestions TEXT;
ALTER TABLE metrics ADD COLUMN design_pattern TEXT;
ALTER TABLE metrics ADD COLUMN edge_cases TEXT;
ALTER TABLE metrics ADD COLUMN performance_issues TEXT;
ALTER TABLE metrics ADD COLUMN best_practices TEXT;
ALTER TABLE metrics ADD COLUMN scalability_analysis TEXT;
ALTER TABLE metrics ADD COLUMN readability_score INT DEFAULT 0;
ALTER TABLE metrics ADD COLUMN maintainability_score INT DEFAULT 0;
