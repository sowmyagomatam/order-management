CREATE TABLE payments (
   id VARCHAR(36) PRIMARY KEY,
   order_id VARCHAR(36) NOT NULL UNIQUE,
   amount DECIMAL(19, 2) NOT NULL,
   payment_status VARCHAR(20) NOT NULL,
   payment_method VARCHAR(50) NOT NULL,
   payment_reference VARCHAR(255),
   card_last_four_digits VARCHAR(4),
   failure_reason VARCHAR(500),
   created_at TIMESTAMP NOT NULL,
   updated_at TIMESTAMP NOT NULL
   );
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

COMMENT ON TABLE payments IS 'Stores payment information for orders';
COMMENT ON COLUMN payments.id IS 'Unique payment identifier';
COMMENT ON COLUMN payments.order_id IS 'Reference to order (one-to-one relationship)';
COMMENT ON COLUMN payments.amount IS 'Payment amount in currency units';
COMMENT ON COLUMN payments.payment_status IS 'Current status: PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED';
COMMENT ON COLUMN payments.payment_reference IS 'External payment gateway reference (e.g., Stripe charge ID)';
COMMENT ON COLUMN payments.card_last_four_digits IS 'Last 4 digits of card for display';