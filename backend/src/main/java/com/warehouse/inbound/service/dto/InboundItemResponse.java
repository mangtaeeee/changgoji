package com.warehouse.inbound.service.dto;

import com.warehouse.inbound.domain.InboundItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "입고 품목 응답")
public record InboundItemResponse(
    @Schema(description = "입고 품목 ID", example = "1")
    Long id,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "상품명", example = "상품A")
    String skuName,
    @Schema(description = "입고 지시 수량", example = "50")
    int orderedQty,
    @Schema(description = "실사 입고 수량", example = "48")
    int receivedQty,
    @Schema(description = "입고 품목 상태", example = "PARTIAL")
    String status
) {

    public static InboundItemResponse from(InboundItem item) {
        return new InboundItemResponse(
            item.getId(),
            item.getSkuId(),
            item.getSkuName(),
            item.getOrderedQty(),
            item.getReceivedQty(),
            item.getStatus().name()
        );
    }
}
