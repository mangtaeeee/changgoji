package com.warehouse.picking.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "피킹 작업 완료 요청")
public record PickingTaskPickRequest(
    @Schema(description = "작업자 ID", example = "999")
    @NotNull Long assignedTo
) {
}
