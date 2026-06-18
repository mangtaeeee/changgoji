-- Variant A: keep current inventory index order.
-- Current production-like index: (warehouse_id, sku_id)

\echo ''
\echo 'Variant A: (warehouse_id, sku_id)'

DROP INDEX IF EXISTS idx_inventory_sku_warehouse;
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_sku
    ON inventory (warehouse_id, sku_id);

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
