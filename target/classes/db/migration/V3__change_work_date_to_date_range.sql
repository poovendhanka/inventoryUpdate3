-- Add from_date column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'labour_entries' AND column_name = 'from_date') THEN
        ALTER TABLE labour_entries ADD COLUMN from_date DATE;
    END IF;
END $$;

-- Add to_date column if it doesn't exist (as nullable first)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'labour_entries' AND column_name = 'to_date') THEN
        ALTER TABLE labour_entries ADD COLUMN to_date DATE;
    END IF;
END $$;

-- Migrate existing data: set from_date and to_date to work_date for existing records
UPDATE labour_entries
SET from_date = work_date
WHERE from_date IS NULL AND work_date IS NOT NULL;

UPDATE labour_entries
SET to_date = work_date
WHERE to_date IS NULL AND work_date IS NOT NULL;

-- If work_date doesn't exist, set both to current date for any remaining NULLs
UPDATE labour_entries
SET from_date = CURRENT_DATE, to_date = CURRENT_DATE
WHERE from_date IS NULL OR to_date IS NULL;

-- Make from_date NOT NULL if it's not already
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'labour_entries' 
               AND column_name = 'from_date' 
               AND is_nullable = 'YES') THEN
        ALTER TABLE labour_entries ALTER COLUMN from_date SET NOT NULL;
    END IF;
END $$;

-- Make to_date NOT NULL if it's not already
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'labour_entries' 
               AND column_name = 'to_date' 
               AND is_nullable = 'YES') THEN
        ALTER TABLE labour_entries ALTER COLUMN to_date SET NOT NULL;
    END IF;
END $$;

-- Add constraint to ensure to_date >= from_date (if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'labour_entries' 
                   AND constraint_name = 'chk_date_range') THEN
        ALTER TABLE labour_entries
            ADD CONSTRAINT chk_date_range CHECK (to_date >= from_date);
    END IF;
END $$;

-- Create indexes for better query performance (if they don't exist)
CREATE INDEX IF NOT EXISTS idx_labour_entries_from_date ON labour_entries(from_date);
CREATE INDEX IF NOT EXISTS idx_labour_entries_to_date ON labour_entries(to_date);
CREATE INDEX IF NOT EXISTS idx_labour_entries_date_range ON labour_entries(from_date, to_date);

