package com.warehouse.inbound.service.dto;

import com.warehouse.inbound.domain.InboundOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "입고 지시 응답")
public record InboundOrderResponse(
    @Schema(description = "입고 지시 ID", example = "1")
    Long id,
    @Schema(description = "입고 상태", example = "REQUESTED")
    String status,
    @Schema(description = "입고 예정일", example = "2026-06-20")
    LocalDate scheduledDate,
    @Schema(description = "입고 품목 목록")
    List<InboundItemResponse> items
) {

    public static InboundOrderResponse from(InboundOrder order) {
        return new InboundOrderResponse(
            order.getId(),
            order.getStatus().name(),
            order.getScheduledDate(),
            order.getItems().stream()
                .map(InboundItemResponse::from)
                .toList()
        );
    }
}
