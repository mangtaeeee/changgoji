package com.warehouse.picking.service.dto;

public record PickingTaskCreateCommand(
    Long outboundItemId,
    Long skuId,
    String locationCode,
    int qty
) {
}
