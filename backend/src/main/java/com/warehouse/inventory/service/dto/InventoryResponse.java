package com.warehouse.inventory.service.dto;

import com.warehouse.inventory.domain.Inventory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "재고 응답")
public record InventoryResponse(
    @Schema(description = "재고 ID", example = "1")
    Long id,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "상품명", example = "상품A")
    String skuName,
    @Schema(description = "출고 가능한 가용 재고 수량", example = "48")
    int availableQty,
    @Schema(description = "출고 지시에 할당된 재고 수량", example = "10")
    int allocatedQty
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId(),
            inventory.getSkuId(),
            inventory.getSkuName(),
            inventory.getAvailableQty(),
            inventory.getAllocatedQty()
        );
    }
}
