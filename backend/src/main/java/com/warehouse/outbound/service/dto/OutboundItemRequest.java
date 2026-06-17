package com.warehouse.outbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "출고 요청 품목")
public record OutboundItemRequest(
    @Schema(description = "SKU ID", example = "100")
    @NotNull Long skuId,
    @Schema(description = "출고 요청 수량", example = "5")
    @Min(1) int requestedQty,
    @Schema(description = "피킹 위치 코드", example = "A-01-03")
    @NotBlank String locationCode
) {
}
