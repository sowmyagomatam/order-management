-- Add cancellation_at column to orders table
ALTER TABLE orders
ADD COLUMN cancelled_at TIMESTAMP;

COMMENT ON COLUMN orders.cancelled_at IS 'Timestamp when the order was cancelled (null if not cancelled)';