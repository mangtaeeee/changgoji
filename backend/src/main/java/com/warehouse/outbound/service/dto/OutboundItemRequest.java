package com.warehouse.outbound.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OutboundItemRequest(
    @NotNull Long skuId,
    @Min(1) int requestedQty,
    @NotBlank String locationCode
) {
}
