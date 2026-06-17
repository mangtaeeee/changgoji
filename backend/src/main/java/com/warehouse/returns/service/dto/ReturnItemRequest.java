package com.warehouse.returns.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "반품 요청 품목")
public record ReturnItemRequest(
    @Schema(description = "SKU ID", example = "100")
    @NotNull Long skuId,
    @Schema(description = "상품명", example = "상품A")
    @NotBlank String skuName,
    @Schema(description = "반품 요청 수량", example = "2")
    @Min(1) int requestedQty
) {
}
