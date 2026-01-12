-- Create order_items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,

    -- Product information (snapshot)
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,

    -- Order details
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,

    -- Additional product metadata (JSON)
    product_metadata TEXT,

    -- Foreign key to orders
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- Constraints
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_unit_price CHECK (unit_price > 0),
    CONSTRAINT chk_subtotal CHECK (subtotal >= 0)
);

-- Indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Comments
COMMENT ON TABLE order_items IS 'Line items for orders';
COMMENT ON COLUMN order_items.product_metadata IS 'JSON metadata: {"category": "Electronics", "brand": "Apple"}';
COMMENT ON COLUMN order_items.subtotal IS 'Calculated as quantity * unit_price';