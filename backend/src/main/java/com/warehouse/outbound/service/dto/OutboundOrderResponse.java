package com.warehouse.outbound.service.dto;

import com.warehouse.outbound.domain.OutboundOrder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "출고 지시 응답")
public record OutboundOrderResponse(
    @Schema(description = "출고 지시 ID", example = "1")
    Long id,
    @Schema(description = "외부 주문 ID", example = "ORDER-20260617-001")
    String orderId,
    @Schema(description = "출고 상태", example = "PENDING")
    String status
) {

    public static OutboundOrderResponse from(OutboundOrder order) {
        return new OutboundOrderResponse(order.getId(), order.getOrderId(), order.getStatus().name());
    }
}
