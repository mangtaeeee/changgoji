package com.warehouse.outbound.service.dto;

import com.warehouse.outbound.domain.OutboundOrder;

public record OutboundOrderResponse(Long id, String orderId, String status) {

    public static OutboundOrderResponse from(OutboundOrder order) {
        return new OutboundOrderResponse(order.getId(), order.getOrderId(), order.getStatus().name());
    }
}
