package com.warehouse.inbound.service.dto;

import com.warehouse.inbound.domain.InboundOrder;
import java.time.LocalDate;

public record InboundOrderResponse(Long id, String status, LocalDate scheduledDate) {

    public static InboundOrderResponse from(InboundOrder order) {
        return new InboundOrderResponse(order.getId(), order.getStatus().name(), order.getScheduledDate());
    }
}
