package com.warehouse.putaway.service.dto;

import com.warehouse.putaway.domain.PutawayTask;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "적치 작업 응답")
public record PutawayTaskResponse(
    @Schema(description = "적치 작업 ID", example = "1")
    Long id,
    @Schema(description = "입고 지시 ID", example = "1")
    Long inboundOrderId,
    @Schema(description = "입고 품목 ID", example = "1")
    Long inboundItemId,
    @Schema(description = "창고 ID", example = "1")
    Long warehouseId,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "상품명", example = "상품A")
    String skuName,
    @Schema(description = "적치 대상 수량", example = "48")
    int qty,
    @Schema(description = "추천 적치 위치", example = "A-01-03")
    String recommendedLocation,
    @Schema(description = "확정 적치 위치", example = "A-01-03")
    String confirmedLocation,
    @Schema(description = "적치 작업 상태", example = "PENDING")
    String status,
    @Schema(description = "작업자 ID", example = "999")
    Long assignedTo
) {

    public static PutawayTaskResponse from(PutawayTask task) {
        return new PutawayTaskResponse(
            task.getId(),
            task.getInboundOrderId(),
            task.getInboundItemId(),
            task.getWarehouseId(),
            task.getSkuId(),
            task.getSkuName(),
            task.getQty(),
            task.getRecommendedLocation(),
            task.getConfirmedLocation(),
            task.getStatus().name(),
            task.getAssignedTo()
        );
    }
}
