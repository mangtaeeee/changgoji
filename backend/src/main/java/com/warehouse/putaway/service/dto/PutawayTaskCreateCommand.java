package com.warehouse.putaway.service.dto;

public record PutawayTaskCreateCommand(
    Long inboundOrderId,
    Long inboundItemId,
    Long warehouseId,
    Long skuId,
    String skuName,
    int qty
) {
}
