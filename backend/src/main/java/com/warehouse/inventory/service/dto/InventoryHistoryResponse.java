package com.warehouse.inventory.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "재고 이력 응답")
public record InventoryHistoryResponse(
    @Schema(description = "재고 이력 ID", example = "1")
    Long id,
    @Schema(description = "재고 ID", example = "1")
    Long inventoryId,
    @Schema(description = "변경 유형", example = "INBOUND")
    String changeType,
    @Schema(description = "변경 전 수량", example = "0")
    int beforeQty,
    @Schema(description = "변경 후 수량", example = "48")
    int afterQty,
    @Schema(description = "변경 수량", example = "48")
    int changeQty,
    @Schema(description = "참조 ID", example = "1")
    Long referenceId,
    @Schema(description = "이력 생성 시각", example = "2026-06-17T10:00:00")
    LocalDateTime createdAt
) {
}
