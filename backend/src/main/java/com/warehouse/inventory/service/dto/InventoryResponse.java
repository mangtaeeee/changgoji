package com.warehouse.inventory.service.dto;

import com.warehouse.inventory.domain.Inventory;

public record InventoryResponse(Long id, Long skuId, String skuName, int availableQty, int allocatedQty) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId(),
            inventory.getSkuId(),
            inventory.getSkuName(),
            inventory.getAvailableQty(),
            inventory.getAllocatedQty()
        );
    }
}
