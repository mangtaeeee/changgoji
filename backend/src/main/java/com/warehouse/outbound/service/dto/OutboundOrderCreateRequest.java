package com.warehouse.outbound.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OutboundOrderCreateRequest(
    @NotNull Long warehouseId,
    @NotBlank String orderId,
    @Valid @NotEmpty List<OutboundItemRequest> items
) {
}
