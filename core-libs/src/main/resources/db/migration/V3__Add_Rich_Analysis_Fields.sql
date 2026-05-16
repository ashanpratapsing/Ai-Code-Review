-- V3__Add_Rich_Analysis_Fields.sql
-- Adds new columns for FAANG-level structured code analysis results

ALTER TABLE metrics 
ADD COLUMN better_approach TEXT,
ADD COLUMN space_complexity VARCHAR(50),
ADD COLUMN faang_insights TEXT;
