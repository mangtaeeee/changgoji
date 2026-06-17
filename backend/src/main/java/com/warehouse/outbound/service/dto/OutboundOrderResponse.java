package com.warehouse.outbound.service.dto;

import com.warehouse.outbound.domain.OutboundOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "출고 지시 응답")
public record OutboundOrderResponse(
    @Schema(description = "출고 지시 ID", example = "1")
    Long id,
    @Schema(description = "외부 주문 ID", example = "ORDER-20260617-001")
    String orderId,
    @Schema(description = "출고 상태", example = "PENDING")
    String status,
    @Schema(description = "출고 품목 목록")
    List<OutboundItemResponse> items
) {

    public static OutboundOrderResponse from(OutboundOrder order) {
        return new OutboundOrderResponse(
            order.getId(),
            order.getOrderId(),
            order.getStatus().name(),
            order.getItems().stream()
                .map(OutboundItemResponse::from)
                .toList()
        );
    }
}
