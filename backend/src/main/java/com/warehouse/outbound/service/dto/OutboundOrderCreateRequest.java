package com.warehouse.outbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "출고 지시 등록 요청")
public record OutboundOrderCreateRequest(
    @Schema(description = "창고 ID", example = "1")
    @NotNull Long warehouseId,
    @Schema(description = "외부 주문 ID", example = "ORDER-20260617-001")
    @NotBlank String orderId,
    @Schema(description = "출고 요청 품목 목록")
    @Valid @NotEmpty List<OutboundItemRequest> items
) {
}
