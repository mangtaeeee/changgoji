-- Restore original inventory index order.

DROP INDEX IF EXISTS idx_inventory_sku_warehouse;
DROP INDEX IF EXISTS idx_inventory_warehouse_sku;

CREATE INDEX idx_inventory_warehouse_sku
    ON inventory (warehouse_id, sku_id);

ANALYZE inventory;
