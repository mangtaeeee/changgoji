package com.warehouse.inbound.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InboundReceivedItemRequest(
    @NotNull Long inboundItemId,
    @Min(0) int receivedQty
) {
}
