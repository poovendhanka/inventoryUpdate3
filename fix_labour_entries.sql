-- Manual fix for labour_entries table
-- Run this SQL script directly on your PostgreSQL database

-- Step 1: Add to_date column as nullable if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'labour_entries' AND column_name = 'to_date') THEN
        ALTER TABLE labour_entries ADD COLUMN to_date DATE;
    END IF;
END $$;

-- Step 2: Populate to_date for any NULL values
UPDATE labour_entries
SET to_date = work_date
WHERE to_date IS NULL AND work_date IS NOT NULL;

-- If work_date doesn't exist or is NULL, use from_date or current date
UPDATE labour_entries
SET to_date = COALESCE(from_date, CURRENT_DATE)
WHERE to_date IS NULL;

-- Step 3: Now make to_date NOT NULL
ALTER TABLE labour_entries
    ALTER COLUMN to_date SET NOT NULL;

-- Step 4: Ensure from_date is also populated and NOT NULL
UPDATE labour_entries
SET from_date = work_date
WHERE from_date IS NULL AND work_date IS NOT NULL;

UPDATE labour_entries
SET from_date = COALESCE(to_date, CURRENT_DATE)
WHERE from_date IS NULL;

ALTER TABLE labour_entries
    ALTER COLUMN from_date SET NOT NULL;

-- Step 5: Add constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'labour_entries' 
                   AND constraint_name = 'chk_date_range') THEN
        ALTER TABLE labour_entries
            ADD CONSTRAINT chk_date_range CHECK (to_date >= from_date);
    END IF;
END $$;

-- Step 6: Create indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_labour_entries_from_date ON labour_entries(from_date);
CREATE INDEX IF NOT EXISTS idx_labour_entries_to_date ON labour_entries(to_date);
CREATE INDEX IF NOT EXISTS idx_labour_entries_date_range ON labour_entries(from_date, to_date);

