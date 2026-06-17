package com.warehouse.returns.service.dto;

import com.warehouse.returns.domain.ReturnItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "반품 품목 응답")
public record ReturnItemResponse(
    @Schema(description = "반품 품목 ID", example = "1")
    Long id,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "상품명", example = "상품A")
    String skuName,
    @Schema(description = "반품 요청 수량", example = "2")
    int requestedQty,
    @Schema(description = "실제 회수 수량", example = "2")
    int receivedQty,
    @Schema(description = "상품 상태", example = "RESELLABLE")
    String condition
) {

    public static ReturnItemResponse from(ReturnItem item) {
        return new ReturnItemResponse(
            item.getId(),
            item.getSkuId(),
            item.getSkuName(),
            item.getRequestedQty(),
            item.getReceivedQty(),
            item.getCondition() == null ? null : item.getCondition().name()
        );
    }
}
