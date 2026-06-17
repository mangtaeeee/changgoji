package com.warehouse.putaway.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "적치 작업 시작 요청")
public record PutawayStartRequest(
    @Schema(description = "작업자 ID", example = "999")
    @NotNull Long assignedTo
) {
}
