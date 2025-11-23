-- Quick fix: Make work_date nullable or drop it
-- Run this SQL directly on your PostgreSQL database

-- Option 1: Make work_date nullable (recommended - keeps data for reference)
ALTER TABLE labour_entries 
ALTER COLUMN work_date DROP NOT NULL;

-- Option 2: Drop work_date column entirely (uncomment if you want to remove it)
-- ALTER TABLE labour_entries DROP COLUMN work_date;

