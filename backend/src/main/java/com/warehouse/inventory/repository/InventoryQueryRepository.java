package com.warehouse.inventory.repository;

import static com.warehouse.inventory.domain.QInventory.inventory;
import static com.warehouse.inventory.domain.QInventoryHistory.inventoryHistory;
import static com.warehouse.inventory.domain.QInventoryLocation.inventoryLocation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.warehouse.inventory.service.dto.InventoryHistoryResponse;
import com.warehouse.inventory.service.dto.InventoryListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<InventoryListResponse> findInventories(Long warehouseId) {
        return queryFactory
            .select(Projections.constructor(
                InventoryListResponse.class,
                inventory.id,
                inventory.warehouseId,
                inventory.skuId,
                inventory.skuName,
                inventory.availableQty,
                inventory.allocatedQty,
                inventoryLocation.locationCode,
                inventoryLocation.qty
            ))
            .from(inventory)
            .leftJoin(inventoryLocation).on(inventoryLocation.inventory.id.eq(inventory.id))
            .where(inventory.warehouseId.eq(warehouseId))
            .orderBy(inventory.skuId.asc(), inventoryLocation.locationCode.asc())
            .fetch();
    }

    public List<InventoryHistoryResponse> findHistories(Long inventoryId) {
        return queryFactory
            .select(Projections.constructor(
                InventoryHistoryResponse.class,
                inventoryHistory.id,
                inventoryHistory.inventory.id,
                inventoryHistory.changeType.stringValue(),
                inventoryHistory.beforeQty,
                inventoryHistory.afterQty,
                inventoryHistory.changeQty,
                inventoryHistory.referenceId,
                inventoryHistory.createdAt
            ))
            .from(inventoryHistory)
            .where(inventoryHistory.inventory.id.eq(inventoryId))
            .orderBy(inventoryHistory.createdAt.desc(), inventoryHistory.id.desc())
            .fetch();
    }

    public List<InventoryHistoryResponse> findHistoriesByOffset(Long inventoryId, int page, int size) {
        return queryFactory
            .select(Projections.constructor(
                InventoryHistoryResponse.class,
                inventoryHistory.id,
                inventoryHistory.inventory.id,
                inventoryHistory.changeType.stringValue(),
                inventoryHistory.beforeQty,
                inventoryHistory.afterQty,
                inventoryHistory.changeQty,
                inventoryHistory.referenceId,
                inventoryHistory.createdAt
            ))
            .from(inventoryHistory)
            .where(inventoryHistory.inventory.id.eq(inventoryId))
            .orderBy(inventoryHistory.id.desc())
            .offset((long) page * size)
            .limit(size)
            .fetch();
    }

    public List<InventoryHistoryResponse> findHistoriesByCursor(Long inventoryId, Long cursor, int size) {
        BooleanBuilder where = new BooleanBuilder()
            .and(inventoryHistory.inventory.id.eq(inventoryId));

        if (cursor != null) {
            where.and(inventoryHistory.id.lt(cursor));
        }

        return queryFactory
            .select(Projections.constructor(
                InventoryHistoryResponse.class,
                inventoryHistory.id,
                inventoryHistory.inventory.id,
                inventoryHistory.changeType.stringValue(),
                inventoryHistory.beforeQty,
                inventoryHistory.afterQty,
                inventoryHistory.changeQty,
                inventoryHistory.referenceId,
                inventoryHistory.createdAt
            ))
            .from(inventoryHistory)
            .where(where)
            .orderBy(inventoryHistory.id.desc())
            .limit(size)
            .fetch();
    }
}
