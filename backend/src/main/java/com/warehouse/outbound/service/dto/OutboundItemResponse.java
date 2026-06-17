package com.warehouse.outbound.service.dto;

import com.warehouse.outbound.domain.OutboundItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "출고 품목 응답")
public record OutboundItemResponse(
    @Schema(description = "출고 품목 ID", example = "1")
    Long id,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "출고 요청 수량", example = "5")
    int requestedQty,
    @Schema(description = "실제 출고 수량", example = "5")
    int shippedQty,
    @Schema(description = "피킹 위치 코드", example = "A-01-03")
    String locationCode
) {

    public static OutboundItemResponse from(OutboundItem item) {
        return new OutboundItemResponse(
            item.getId(),
            item.getSkuId(),
            item.getRequestedQty(),
            item.getShippedQty(),
            item.getLocationCode()
        );
    }
}
