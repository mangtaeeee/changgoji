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

CREATE INDEX IF NOT EXISTS idx_inventory_location_inventory_location_code
    ON inventory_location (inventory_id, location_code);

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

CREATE TABLE IF NOT EXISTS putaway_task (
    id BIGSERIAL PRIMARY KEY,
    inbound_order_id BIGINT NOT NULL REFERENCES inbound_order(id),
    inbound_item_id BIGINT NOT NULL REFERENCES inbound_item(id),
    warehouse_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    qty INTEGER NOT NULL,
    recommended_location VARCHAR(255) NOT NULL,
    confirmed_location VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    assigned_to BIGINT,
    created_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_putaway_warehouse_status ON putaway_task (warehouse_id, status);

CREATE TABLE IF NOT EXISTS picking_wave (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    outbound_order_id BIGINT NOT NULL REFERENCES outbound_order(id),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_picking_wave_warehouse_status ON picking_wave (warehouse_id, status);

CREATE TABLE IF NOT EXISTS picking_task (
    id BIGSERIAL PRIMARY KEY,
    wave_id BIGINT NOT NULL REFERENCES picking_wave(id),
    outbound_item_id BIGINT NOT NULL REFERENCES outbound_item(id),
    sku_id BIGINT NOT NULL,
    location_code VARCHAR(255) NOT NULL,
    qty INTEGER NOT NULL,
    assigned_to BIGINT,
    status VARCHAR(30) NOT NULL,
    picked_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipping_label (
    id BIGSERIAL PRIMARY KEY,
    outbound_order_id BIGINT NOT NULL REFERENCES outbound_order(id),
    tracking_no VARCHAR(255) NOT NULL UNIQUE,
    carrier VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    receiver_phone VARCHAR(255) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    label_data TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP,
    print_requested_at TIMESTAMP,
    printed_at TIMESTAMP,
    failure_reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_shipping_label_outbound_order ON shipping_label (outbound_order_id);
