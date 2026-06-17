package com.warehouse.putaway.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "적치 확정 요청")
public record PutawayConfirmRequest(
    @Schema(description = "작업자가 스캔하거나 확정한 실제 적치 위치", example = "A-01-03")
    @NotBlank String confirmedLocation
) {
}
