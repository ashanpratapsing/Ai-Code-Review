-- V2__Extend_Metrics_For_AI.sql
-- Adds fields to store rich AI-generated code reviews

ALTER TABLE metrics 
ADD COLUMN summary TEXT,
ADD COLUMN bugs TEXT,
ADD COLUMN optimization TEXT,
ADD COLUMN code_smells TEXT,
ADD COLUMN time_complexity VARCHAR(50),
ADD COLUMN refactored_code TEXT,
ADD COLUMN unit_tests TEXT;
