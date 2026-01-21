-- Create orders table
CREATE TABLE products (
    product_id VARCHAR(36) PRIMARY KEY NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(36) NOT NULL UNIQUE,
    description VARCHAR(255),
    price NUMERIC(10, 2) NOT NULL,
    available_quantity INTEGER DEFAULT 0 NOT NULL,
    reserved_quantity INTEGER DEFAULT 0 NOT NULL,
    -- Audit fields
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes for common queries
CREATE INDEX idx_products_name ON products(product_name);
CREATE INDEX idx_products_sku ON products(product_sku);
CREATE INDEX idx_products_created_at ON products(created_at);


-- Comments for documentation
COMMENT ON TABLE products IS 'Main products table with inventory management';
COMMENT ON COLUMN products.id IS 'UUID primary key';
COMMENT ON COLUMN products.available_quantity IS 'Quantity available for new orders';
COMMENT ON COLUMN products.reserved_quantity IS 'Quantity reserved for pending orders';