-- Add deleted column to parties table
ALTER TABLE parties ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add deleted column to dealers table
ALTER TABLE dealers ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE; 