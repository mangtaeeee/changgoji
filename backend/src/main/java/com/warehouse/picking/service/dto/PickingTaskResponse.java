package com.warehouse.picking.service.dto;

import com.warehouse.picking.domain.PickingTask;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피킹 작업 응답")
public record PickingTaskResponse(
    @Schema(description = "피킹 작업 ID", example = "1")
    Long id,
    @Schema(description = "피킹 웨이브 ID", example = "1")
    Long waveId,
    @Schema(description = "출고 품목 ID", example = "1")
    Long outboundItemId,
    @Schema(description = "SKU ID", example = "100")
    Long skuId,
    @Schema(description = "피킹 위치", example = "A-01-03")
    String locationCode,
    @Schema(description = "피킹 수량", example = "5")
    int qty,
    @Schema(description = "작업자 ID", example = "999")
    Long assignedTo,
    @Schema(description = "피킹 작업 상태", example = "PENDING")
    String status
) {

    public static PickingTaskResponse from(PickingTask task) {
        return new PickingTaskResponse(
            task.getId(),
            task.getWave().getId(),
            task.getOutboundItemId(),
            task.getSkuId(),
            task.getLocationCode(),
            task.getQty(),
            task.getAssignedTo(),
            task.getStatus().name()
        );
    }
}
