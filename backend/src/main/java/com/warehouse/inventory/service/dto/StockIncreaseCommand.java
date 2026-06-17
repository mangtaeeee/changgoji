package com.warehouse.inventory.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockIncreaseCommand(
    @NotNull Long warehouseId,
    @NotNull Long skuId,
    @NotBlank String skuName,
    @Min(1) int qty,
    @NotNull Long referenceId
) {
}
