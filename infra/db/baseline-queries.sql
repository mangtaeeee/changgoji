-- Inventory index baseline queries
-- Compare query plans with EXPLAIN ANALYZE.

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
