package com.warehouse.picking.service.dto;

import com.warehouse.picking.domain.PickingWave;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "피킹 웨이브 응답")
public record PickingWaveResponse(
    @Schema(description = "피킹 웨이브 ID", example = "1")
    Long id,
    @Schema(description = "창고 ID", example = "1")
    Long warehouseId,
    @Schema(description = "출고 지시 ID", example = "1")
    Long outboundOrderId,
    @Schema(description = "피킹 웨이브 상태", example = "OPEN")
    String status,
    @Schema(description = "피킹 작업 목록")
    List<PickingTaskResponse> tasks
) {

    public static PickingWaveResponse from(PickingWave wave) {
        return new PickingWaveResponse(
            wave.getId(),
            wave.getWarehouseId(),
            wave.getOutboundOrderId(),
            wave.getStatus().name(),
            wave.getTasks().stream()
                .map(PickingTaskResponse::from)
                .toList()
        );
    }
}
