package com.warehouse.returns.service.dto;

import com.warehouse.returns.domain.ReturnReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "반품 접수 요청")
public record ReturnOrderCreateRequest(
    @Schema(description = "원 출고 지시 ID", example = "1")
    @NotNull Long outboundOrderId,
    @Schema(description = "창고 ID", example = "1")
    @NotNull Long warehouseId,
    @Schema(description = "반품 사유", example = "CUSTOMER_CHANGE")
    @NotNull ReturnReason reason,
    @Schema(description = "반품 요청 품목 목록")
    @Valid @NotEmpty List<ReturnItemRequest> items
) {
}
