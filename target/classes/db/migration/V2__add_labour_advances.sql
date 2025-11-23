-- Create labour_advances table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'labour_advances') THEN
        CREATE TABLE labour_advances (
            id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
            employee_id BIGINT NOT NULL,
            amount DECIMAL(12,2) NOT NULL,
            remaining_amount DECIMAL(12,2) NOT NULL,
            advance_date DATE NOT NULL,
            remarks VARCHAR(500),
            settled BOOLEAN NOT NULL DEFAULT FALSE,
            entry_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_labour_adv_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
        );
    END IF;
END $$;

-- Add advance adjustment columns to labour_entries table if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'labour_entries' AND column_name = 'advance_adjustment') THEN
        ALTER TABLE labour_entries
            ADD COLUMN advance_adjustment DECIMAL(12,2) NOT NULL DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'labour_entries' AND column_name = 'net_payable') THEN
        ALTER TABLE labour_entries
            ADD COLUMN net_payable DECIMAL(12,2) NOT NULL DEFAULT 0;
    END IF;
END $$;

-- Create indexes for better query performance (if they don't exist)
CREATE INDEX IF NOT EXISTS idx_labour_advances_employee ON labour_advances(employee_id);
CREATE INDEX IF NOT EXISTS idx_labour_advances_settled ON labour_advances(settled);
CREATE INDEX IF NOT EXISTS idx_labour_advances_date ON labour_advances(advance_date);

