package com.warehouse.picking.service.dto;

import java.util.List;

public record PickingWaveCreateCommand(
    Long warehouseId,
    Long outboundOrderId,
    List<PickingTaskCreateCommand> tasks
) {
}
