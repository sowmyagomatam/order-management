-- Add cancellation_reason column to orders table
ALTER TABLE orders
ADD COLUMN cancellation_reason VARCHAR(50);

-- Add index for querying cancelled orders by reason
CREATE INDEX idx_orders_cancellation_reason
ON orders(status, cancellation_reason)
WHERE status = 'CANCELLED';