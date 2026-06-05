-- Create reservations table: tracks stock reserved per order item.
-- Only fully-fulfilled orders get rows here (all-or-nothing reservation).
CREATE TABLE reservations (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_sku VARCHAR(36),
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    -- Audit fields
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes for reconciliation/release lookups by order
CREATE INDEX idx_reservations_order_id ON reservations(order_id);
CREATE INDEX idx_reservations_order_id_status ON reservations(order_id, status);

-- Comments for documentation
COMMENT ON TABLE reservations IS 'Stock reserved per order item; source of truth for inventory compensation';
COMMENT ON COLUMN reservations.order_id IS 'Order the reservation belongs to';
COMMENT ON COLUMN reservations.status IS 'RESERVED | RELEASED | CONFIRMED';
