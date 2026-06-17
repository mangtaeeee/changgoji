package com.warehouse.inventory.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockLocationDecreaseCommand(
    @NotNull Long warehouseId,
    @NotNull Long skuId,
    @NotBlank String locationCode,
    @Min(1) int qty
) {
}
