package com.warehouse.inventory.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "재고 조정 요청")
public record InventoryAdjustRequest(
    @Schema(description = "재고 ID", example = "1")
    @NotNull Long inventoryId,
    @Schema(description = "조정 수량. 증가면 양수, 감소면 음수", example = "-2")
    int adjustQty,
    @Schema(description = "재고 조정 사유", example = "파손으로 인한 폐기")
    String reason
) {
}
