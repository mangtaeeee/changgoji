package com.warehouse.seed;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SplittableRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class InventorySeedRunner implements ApplicationRunner {

    private static final int WAREHOUSE_COUNT = 5;
    private static final int SKU_COUNT = 20_000;
    private static final int HISTORY_PER_INVENTORY = 5;
    private static final int INVENTORY_BATCH_SIZE = 1_000;
    private static final int HISTORY_BATCH_SIZE = 5_000;
    private static final int LOG_UNIT = 10_000;
    private static final String[] CHANGE_TYPES = {
        "INBOUND",
        "OUTBOUND",
        "ALLOCATE",
        "RELEASE",
        "RETURN_INBOUND",
        "DEFECTIVE_RETURN",
        "ADJUST"
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Long inventoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory", Long.class);
        if (inventoryCount != null && inventoryCount > 0) {
            log.info("inventory table already has {} rows. skip seed.", inventoryCount);
            return;
        }

        log.info("start inventory seed. inventories={}, histories={}", WAREHOUSE_COUNT * SKU_COUNT,
            WAREHOUSE_COUNT * SKU_COUNT * HISTORY_PER_INVENTORY);
        insertInventories();
        insertInventoryHistories();
        log.info("inventory seed finished.");
    }

    private void insertInventories() {
        SplittableRandom random = new SplittableRandom(20260618L);
        int totalInventoryCount = WAREHOUSE_COUNT * SKU_COUNT;
        int inserted = 0;
        List<InventorySeedRow> batch = new java.util.ArrayList<>(INVENTORY_BATCH_SIZE);

        for (long warehouseId = 1; warehouseId <= WAREHOUSE_COUNT; warehouseId++) {
            for (long skuId = 1; skuId <= SKU_COUNT; skuId++) {
                batch.add(new InventorySeedRow(
                    warehouseId,
                    skuId,
                    "SEED-SKU-" + skuId,
                    random.nextInt(0, 1_001),
                    random.nextInt(0, 1_001)
                ));
                if (batch.size() == INVENTORY_BATCH_SIZE) {
                    insertInventoryBatch(batch);
                    inserted += batch.size();
                    batch.clear();
                    if (inserted % LOG_UNIT == 0) {
                        log.info("inventory seed progress: {}/{}", inserted, totalInventoryCount);
                    }
                }
            }
        }

        if (!batch.isEmpty()) {
            insertInventoryBatch(batch);
            inserted += batch.size();
            log.info("inventory seed progress: {}/{}", inserted, totalInventoryCount);
        }
    }

    private void insertInventoryBatch(List<InventorySeedRow> rows) {
        jdbcTemplate.batchUpdate("""
            INSERT INTO inventory (
                warehouse_id,
                sku_id,
                sku_name,
                available_qty,
                allocated_qty,
                version,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, 0, now(), now())
            """,
            rows,
            INVENTORY_BATCH_SIZE,
            inventorySetter()
        );
    }

    private ParameterizedPreparedStatementSetter<InventorySeedRow> inventorySetter() {
        return (PreparedStatement ps, InventorySeedRow row) -> {
            ps.setLong(1, row.warehouseId());
            ps.setLong(2, row.skuId());
            ps.setString(3, row.skuName());
            ps.setInt(4, row.availableQty());
            ps.setInt(5, row.allocatedQty());
        };
    }

    private void insertInventoryHistories() {
        List<Long> inventoryIds = jdbcTemplate.queryForList("SELECT id FROM inventory ORDER BY id", Long.class);
        SplittableRandom random = new SplittableRandom(20260618L);
        int totalHistoryCount = inventoryIds.size() * HISTORY_PER_INVENTORY;
        int inserted = 0;

        for (int start = 0; start < inventoryIds.size(); start += HISTORY_BATCH_SIZE / HISTORY_PER_INVENTORY) {
            int end = Math.min(start + HISTORY_BATCH_SIZE / HISTORY_PER_INVENTORY, inventoryIds.size());
            List<InventoryHistorySeedRow> rows = inventoryIds.subList(start, end).stream()
                .flatMap(inventoryId -> java.util.stream.IntStream.range(0, HISTORY_PER_INVENTORY)
                    .mapToObj(ignored -> historyRow(inventoryId, random)))
                .toList();

            jdbcTemplate.batchUpdate("""
                    INSERT INTO inventory_history (
                        inventory_id,
                        change_type,
                        before_qty,
                        after_qty,
                        change_qty,
                        reference_id,
                        created_at
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                rows,
                HISTORY_BATCH_SIZE,
                historySetter()
            );

            inserted += rows.size();
            if (inserted % LOG_UNIT == 0 || inserted == totalHistoryCount) {
                log.info("inventory_history seed progress: {}/{}", inserted, totalHistoryCount);
            }
        }
    }

    private InventoryHistorySeedRow historyRow(Long inventoryId, SplittableRandom random) {
        int beforeQty = random.nextInt(0, 1_001);
        int changeQty = random.nextInt(-200, 201);
        int afterQty = Math.max(0, beforeQty + changeQty);
        LocalDateTime createdAt = LocalDateTime.now().minusSeconds(random.nextLong(0, 90L * 24 * 60 * 60));
        return new InventoryHistorySeedRow(
            inventoryId,
            CHANGE_TYPES[random.nextInt(CHANGE_TYPES.length)],
            beforeQty,
            afterQty,
            afterQty - beforeQty,
            random.nextLong(1, 1_000_001),
            Timestamp.valueOf(createdAt)
        );
    }

    private ParameterizedPreparedStatementSetter<InventoryHistorySeedRow> historySetter() {
        return (PreparedStatement ps, InventoryHistorySeedRow row) -> {
            ps.setLong(1, row.inventoryId());
            ps.setString(2, row.changeType());
            ps.setInt(3, row.beforeQty());
            ps.setInt(4, row.afterQty());
            ps.setInt(5, row.changeQty());
            ps.setLong(6, row.referenceId());
            ps.setTimestamp(7, row.createdAt());
        };
    }

    private record InventorySeedRow(
        long warehouseId,
        long skuId,
        String skuName,
        int availableQty,
        int allocatedQty
    ) {
    }

    private record InventoryHistorySeedRow(
        long inventoryId,
        String changeType,
        int beforeQty,
        int afterQty,
        int changeQty,
        long referenceId,
        Timestamp createdAt
    ) {
    }
}
