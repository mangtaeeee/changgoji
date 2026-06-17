package com.warehouse.inventory.service.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryAdjustRequest(
    @NotNull Long inventoryId,
    int adjustQty,
    String reason
) {
}
