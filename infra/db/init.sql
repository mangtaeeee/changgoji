CREATE TABLE IF NOT EXISTS inbound_order (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    scheduled_date DATE NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inbound_item (
    id BIGSERIAL PRIMARY KEY,
    inbound_order_id BIGINT NOT NULL REFERENCES inbound_order(id),
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    ordered_qty INTEGER NOT NULL,
    received_qty INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS inbound_receipt (
    id BIGSERIAL PRIMARY KEY,
    inbound_order_id BIGINT NOT NULL UNIQUE REFERENCES inbound_order(id),
    confirmed_by BIGINT NOT NULL,
    confirmed_at TIMESTAMP NOT NULL,
    memo TEXT
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    available_qty INTEGER NOT NULL,
    allocated_qty INTEGER NOT NULL,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_sku ON inventory (warehouse_id, sku_id);

CREATE TABLE IF NOT EXISTS inventory_location (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory(id),
    location_code VARCHAR(255) NOT NULL,
    qty INTEGER NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_history (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory(id),
    change_type VARCHAR(30) NOT NULL,
    before_qty INTEGER NOT NULL,
    after_qty INTEGER NOT NULL,
    change_qty INTEGER NOT NULL,
    reference_id BIGINT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbound_order (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    shipped_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbound_item (
    id BIGSERIAL PRIMARY KEY,
    outbound_order_id BIGINT NOT NULL REFERENCES outbound_order(id),
    sku_id BIGINT NOT NULL,
    requested_qty INTEGER NOT NULL,
    shipped_qty INTEGER NOT NULL,
    location_code VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS return_order (
    id BIGSERIAL PRIMARY KEY,
    outbound_order_id BIGINT NOT NULL REFERENCES outbound_order(id),
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(30) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS return_item (
    id BIGSERIAL PRIMARY KEY,
    return_order_id BIGINT NOT NULL REFERENCES return_order(id),
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    requested_qty INTEGER NOT NULL,
    received_qty INTEGER NOT NULL,
    condition VARCHAR(30)
);
