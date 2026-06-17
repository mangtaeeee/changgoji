package com.warehouse.inbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "입고 확정 요청")
public record InboundConfirmRequest(
    @Schema(description = "입고 확정 작업자 ID", example = "999")
    @NotNull Long confirmedBy,
    @Schema(description = "입고 확정 메모", example = "일부 파손 1개 제외")
    String memo
) {
}
