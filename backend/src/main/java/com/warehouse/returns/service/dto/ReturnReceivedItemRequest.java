package com.warehouse.returns.service.dto;

import com.warehouse.returns.domain.ItemCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "반품 품목별 실사 결과")
public record ReturnReceivedItemRequest(
    @Schema(description = "반품 품목 ID", example = "1")
    @NotNull Long returnItemId,
    @Schema(description = "실제로 회수된 수량", example = "2")
    @Min(0) int receivedQty,
    @Schema(description = "상품 상태. RESELLABLE은 재고 복구, DEFECTIVE는 불량 이력 기록", example = "RESELLABLE")
    @NotNull ItemCondition condition
) {
}
