package com.warehouse.inventory.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "재고 목록 응답")
public record InventoryListResponse(
    @Schema(description = "재고 ID", example = "1")
    Long id,
    @Schema(description = "창고 ID", example = "1")
    Long warehouseId,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "상품명", example = "상품A")
    String skuName,
    @Schema(description = "출고 가능한 가용 재고 수량", example = "48")
    int availableQty,
    @Schema(description = "출고 지시에 할당된 재고 수량", example = "5")
    int allocatedQty,
    @Schema(description = "적치 위치 코드", example = "A-01-03")
    String locationCode,
    @Schema(description = "해당 위치 수량", example = "43")
    Integer locationQty
) {
}
