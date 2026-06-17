package com.warehouse.returns.service.dto;

import com.warehouse.returns.domain.ReturnOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "반품 지시 응답")
public record ReturnOrderResponse(
    @Schema(description = "반품 지시 ID", example = "1")
    Long id,
    @Schema(description = "원 출고 지시 ID", example = "1")
    Long outboundOrderId,
    @Schema(description = "창고 ID", example = "1")
    Long warehouseId,
    @Schema(description = "반품 상태", example = "REQUESTED")
    String status,
    @Schema(description = "반품 사유", example = "CUSTOMER_CHANGE")
    String reason,
    @Schema(description = "반품 품목 목록")
    List<ReturnItemResponse> items
) {

    public static ReturnOrderResponse from(ReturnOrder order) {
        return new ReturnOrderResponse(
            order.getId(),
            order.getOutboundOrderId(),
            order.getWarehouseId(),
            order.getStatus().name(),
            order.getReason().name(),
            order.getItems().stream()
                .map(ReturnItemResponse::from)
                .toList()
        );
    }
}
