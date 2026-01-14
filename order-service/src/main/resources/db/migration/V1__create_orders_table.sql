-- Create orders table
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,

    -- Shipping address (embedded)
    shipping_street VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(50),
    shipping_zip_code VARCHAR(20),
    shipping_country VARCHAR(50),

    -- Billing address (embedded)
    billing_street VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(50),
    billing_zip_code VARCHAR(20),
    billing_country VARCHAR(50),

    -- Audit fields
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_status CHECK (status IN (
        'PENDING',
        'INVENTORY_RESERVED',
        'PAYMENT_PROCESSING',
        'PAYMENT_COMPLETED',
        'CONFIRMED',
        'SHIPPED',
        'DELIVERED',
        'CANCELLED',
        'FAILED'
    ))
);

-- Indexes for common queries
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);

-- Comments for documentation
COMMENT ON TABLE orders IS 'Main orders table storing customer orders';
COMMENT ON COLUMN orders.id IS 'UUID primary key';
COMMENT ON COLUMN orders.status IS 'Order lifecycle status';
COMMENT ON COLUMN orders.total_amount IS 'Total order amount in decimal(10,2)';