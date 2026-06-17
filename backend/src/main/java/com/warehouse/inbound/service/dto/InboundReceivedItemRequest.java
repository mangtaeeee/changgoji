package com.warehouse.inbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "입고 품목별 실사 수량")
public record InboundReceivedItemRequest(
    @Schema(description = "입고 품목 ID", example = "1")
    @NotNull Long inboundItemId,
    @Schema(description = "현장에서 확인한 실제 입고 수량", example = "48")
    @Min(0) int receivedQty
) {
}
