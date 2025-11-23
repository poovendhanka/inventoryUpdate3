-- Manual Flyway repair - Update checksum for V3 migration
-- Run this SQL to fix the checksum mismatch

-- Option 1: Update the checksum in flyway_schema_history table
UPDATE flyway_schema_history 
SET checksum = -2120004383  -- New checksum for the current V3 file
WHERE version = '3' AND description = 'change work date to date range';

-- Option 2: If the above doesn't work, you can delete and re-insert the migration record
-- DELETE FROM flyway_schema_history WHERE version = '3';
-- Then restart the app and Flyway will re-apply it (but it's idempotent so it's safe)

-- Option 3: Use Flyway repair command (recommended)
-- Run: mvn flyway:repair
-- Or use the repair-on-migrate=true setting in application.properties

