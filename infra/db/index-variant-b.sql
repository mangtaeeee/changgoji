-- Variant B: reverse inventory index order.
-- Experimental index: (sku_id, warehouse_id)

\echo ''
\echo 'Variant B: (sku_id, warehouse_id)'

DROP INDEX IF EXISTS idx_inventory_warehouse_sku;
DROP INDEX IF EXISTS idx_inventory_sku_warehouse;
CREATE INDEX idx_inventory_sku_warehouse
    ON inventory (sku_id, warehouse_id);

ANALYZE inventory;

\echo ''
\echo 'Query 1: warehouse inventory scan'
EXPLAIN ANALYZE
SELECT *
FROM inventory
WHERE warehouse_id = 3;

\echo ''
\echo 'Query 2: warehouse + SKU point lookup'
EXPLAIN ANALYZE
SELECT *
FROM inventory
WHERE warehouse_id = 3
  AND sku_id = 15000;

\echo ''
\echo 'Query 3: SKU inventory across warehouses'
EXPLAIN ANALYZE
SELECT *
FROM inventory
WHERE sku_id = 15000;
