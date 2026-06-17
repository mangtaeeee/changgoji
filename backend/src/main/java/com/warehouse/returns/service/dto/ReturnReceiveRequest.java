package com.warehouse.returns.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "반품 실사 처리 요청")
public record ReturnReceiveRequest(
    @Schema(description = "실사 처리할 반품 품목 목록")
    @Valid @NotEmpty List<ReturnReceivedItemRequest> items
) {
}
