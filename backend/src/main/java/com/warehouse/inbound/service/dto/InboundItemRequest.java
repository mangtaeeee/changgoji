package com.warehouse.inbound.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InboundItemRequest(
    @NotNull Long skuId,
    @NotBlank String skuName,
    @Min(1) int orderedQty
) {
}
