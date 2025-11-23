-- Drop work_date column since we're now using from_date and to_date
-- This migration documents the removal of the old work_date column
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'labour_entries' AND column_name = 'work_date') THEN
        ALTER TABLE labour_entries DROP COLUMN work_date;
    END IF;
END $$;

