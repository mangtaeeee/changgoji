package com.warehouse.inventory.service;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;
import com.warehouse.inventory.domain.ChangeType;
import com.warehouse.inventory.domain.Inventory;
import com.warehouse.inventory.domain.InventoryHistory;
import com.warehouse.inventory.repository.InventoryHistoryRepository;
import com.warehouse.inventory.repository.InventoryRepository;
import com.warehouse.inventory.service.dto.InventoryAdjustRequest;
import com.warehouse.inventory.service.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    public InventoryResponse getInventory(Long warehouseId, Long skuId) {
        return InventoryResponse.from(inventoryRepository.findByWarehouseIdAndSkuId(warehouseId, skuId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK)));
    }

    @Transactional
    public InventoryResponse adjustInventory(InventoryAdjustRequest request) {
        Inventory inventory = inventoryRepository.findById(request.inventoryId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK));
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
}
