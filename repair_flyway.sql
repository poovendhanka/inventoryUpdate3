-- Repair Flyway migration history
-- Run this SQL if Flyway still complains about migrations

-- Check current Flyway schema history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- If V2__add_labour_advances.sql migration is not in the history but the table exists,
-- you can manually insert it (optional - Flyway repair is better)

-- Option 1: Use Flyway repair command (recommended)
-- Just run: mvn flyway:repair
-- Or add this to application.properties temporarily:
-- spring.flyway.repair-on-migrate=true

-- Option 2: Manually mark migration as applied (if needed)
-- Only do this if you're sure the migration was already applied
-- INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
-- VALUES (
--     (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
--     '2',
--     'add labour advances',
--     'SQL',
--     'V2__add_labour_advances.sql',
--     0,
--     'postgres',
--     CURRENT_TIMESTAMP,
--     0,
--     true
-- );

