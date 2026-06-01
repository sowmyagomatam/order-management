-- Add cancellation_by column to orders table
ALTER TABLE orders
ADD COLUMN cancelled_by VARCHAR(50);
