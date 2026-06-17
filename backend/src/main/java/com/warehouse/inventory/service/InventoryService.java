package com.warehouse.inventory.service;

import com.warehouse.inventory.domain.ChangeType;
import com.warehouse.inventory.domain.Inventory;
import com.warehouse.inventory.domain.InventoryHistory;
import com.warehouse.inventory.domain.InventoryLocation;
import com.warehouse.inventory.exception.ConcurrentStockUpdateException;
import com.warehouse.inventory.exception.InsufficientStockException;
import com.warehouse.inventory.repository.InventoryHistoryRepository;
import com.warehouse.inventory.repository.InventoryLocationRepository;
import com.warehouse.inventory.repository.InventoryQueryRepository;
import com.warehouse.inventory.repository.InventoryRepository;
import com.warehouse.inventory.service.dto.InventoryAdjustRequest;
import com.warehouse.inventory.service.dto.InventoryHistoryResponse;
import com.warehouse.inventory.service.dto.InventoryListResponse;
import com.warehouse.inventory.service.dto.InventoryResponse;
import com.warehouse.inventory.service.dto.StockIncreaseCommand;
import com.warehouse.inventory.service.dto.StockLocationDecreaseCommand;
import com.warehouse.inventory.service.dto.StockLocationCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final InventoryLocationRepository inventoryLocationRepository;
    private final InventoryQueryRepository inventoryQueryRepository;

    public InventoryResponse getInventory(Long warehouseId, Long skuId) {
        return InventoryResponse.from(inventoryRepository.findByWarehouseIdAndSkuId(warehouseId, skuId)
            .orElseThrow(InsufficientStockException::new));
    }

    public List<InventoryListResponse> getInventories(Long warehouseId) {
        return inventoryQueryRepository.findInventories(warehouseId);
    }

    public List<InventoryHistoryResponse> getInventoryHistories(Long inventoryId) {
        return inventoryQueryRepository.findHistories(inventoryId);
    }

    @Transactional
    public InventoryResponse adjustInventory(InventoryAdjustRequest request) {
        Inventory inventory = inventoryRepository.findById(request.inventoryId())
            .orElseThrow(InsufficientStockException::new);
        int beforeQty = inventory.getAvailableQty();
        inventory.adjust(request.adjustQty());
        inventoryHistoryRepository.save(InventoryHistory.create(
            inventory,
            ChangeType.ADJUST,
            beforeQty,
            inventory.getAvailableQty(),
            request.adjustQty(),
            inventory.getId()
        ));
        return InventoryResponse.from(inventory);
    }

    @Transactional
    public void increaseStock(StockIncreaseCommand command, ChangeType changeType) {
        Inventory inventory = inventoryRepository.findByWarehouseIdAndSkuId(command.warehouseId(), command.skuId())
            .orElseGet(() -> inventoryRepository.save(
                Inventory.create(command.warehouseId(), command.skuId(), command.skuName(), 0)
            ));
        int beforeQty = inventory.getAvailableQty();
        inventory.increase(command.qty());
        saveHistory(inventory, changeType, beforeQty, inventory.getAvailableQty(), command.qty(), command.referenceId());
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public void allocateStock(Long warehouseId, Long skuId, int qty, Long referenceId) {
        Inventory inventory = getInventoryEntity(warehouseId, skuId);
        int beforeQty = inventory.getAvailableQty();
        inventory.allocate(qty);
        saveHistory(inventory, ChangeType.ALLOCATE, beforeQty, inventory.getAvailableQty(), -qty, referenceId);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public void shipStock(Long warehouseId, Long skuId, int qty, Long referenceId) {
        Inventory inventory = getInventoryEntity(warehouseId, skuId);
        int beforeQty = inventory.getAllocatedQty();
        inventory.ship(qty);
        saveHistory(inventory, ChangeType.OUTBOUND, beforeQty, inventory.getAllocatedQty(), -qty, referenceId);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public void releaseStock(Long warehouseId, Long skuId, int qty, Long referenceId) {
        Inventory inventory = getInventoryEntity(warehouseId, skuId);
        int beforeQty = inventory.getAvailableQty();
        inventory.release(qty);
        saveHistory(inventory, ChangeType.RELEASE, beforeQty, inventory.getAvailableQty(), qty, referenceId);
    }

    @Recover
    public void recoverStockUpdate(OptimisticLockingFailureException e, Long warehouseId, Long skuId, int qty,
        Long referenceId) {
        throw new ConcurrentStockUpdateException();
    }

    @Transactional
    public void recordDefectiveReturn(Long warehouseId, Long skuId, Long referenceId) {
        Inventory inventory = getInventoryEntity(warehouseId, skuId);
        saveHistory(inventory, ChangeType.DEFECTIVE_RETURN, inventory.getAvailableQty(), inventory.getAvailableQty(), 0,
            referenceId);
    }

    @Transactional
    public void increaseLocationStock(StockLocationCommand command) {
        Inventory inventory = getInventoryEntity(command.warehouseId(), command.skuId());
        InventoryLocation location = inventoryLocationRepository
            .findByInventoryIdAndLocationCode(inventory.getId(), command.locationCode())
            .orElseGet(() -> inventoryLocationRepository.save(
                InventoryLocation.create(inventory, command.locationCode(), 0)
            ));
        location.increase(command.qty());
    }

    @Transactional
    public void decreaseLocationStock(StockLocationDecreaseCommand command) {
        Inventory inventory = getInventoryEntity(command.warehouseId(), command.skuId());
        InventoryLocation location = inventoryLocationRepository
            .findByInventoryIdAndLocationCode(inventory.getId(), command.locationCode())
            .orElseThrow(InsufficientStockException::new);
        location.decrease(command.qty());
    }

    private Inventory getInventoryEntity(Long warehouseId, Long skuId) {
        return inventoryRepository.findByWarehouseIdAndSkuId(warehouseId, skuId)
            .orElseThrow(InsufficientStockException::new);
    }

    private void saveHistory(Inventory inventory, ChangeType changeType, int beforeQty, int afterQty, int changeQty,
        Long referenceId) {
        inventoryHistoryRepository.save(InventoryHistory.create(
            inventory,
            changeType,
            beforeQty,
            afterQty,
            changeQty,
            referenceId
        ));
    }
}
