package com.warehouse.inbound.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record InboundOrderCreateRequest(
    @NotNull Long warehouseId,
    @NotNull Long supplierId,
    @NotNull LocalDate scheduledDate,
    @Valid @NotEmpty List<InboundItemRequest> items
) {
}
