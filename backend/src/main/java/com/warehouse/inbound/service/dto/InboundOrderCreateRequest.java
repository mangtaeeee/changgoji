package com.warehouse.inbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "입고 지시 등록 요청")
public record InboundOrderCreateRequest(
    @Schema(description = "창고 ID", example = "1")
    @NotNull Long warehouseId,
    @Schema(description = "공급사 ID", example = "10")
    @NotNull Long supplierId,
    @Schema(description = "입고 예정일", example = "2026-06-20")
    @NotNull LocalDate scheduledDate,
    @Schema(description = "입고 예정 품목 목록")
    @Valid @NotEmpty List<InboundItemRequest> items
) {
}
