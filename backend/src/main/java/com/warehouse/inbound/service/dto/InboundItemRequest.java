package com.warehouse.inbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "입고 예정 품목")
public record InboundItemRequest(
    @Schema(description = "SKU ID", example = "100")
    @NotNull Long skuId,
    @Schema(description = "상품명", example = "상품A")
    @NotBlank String skuName,
    @Schema(description = "입고 지시 수량", example = "50")
    @Min(1) int orderedQty
) {
}
