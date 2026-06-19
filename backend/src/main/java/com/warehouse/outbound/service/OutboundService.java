package com.warehouse.outbound.service;

import com.warehouse.common.exception.InvalidStatusException;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.outbound.domain.OutboundOrder;
import com.warehouse.outbound.domain.OutboundStatus;
import com.warehouse.outbound.exception.OutboundOrderNotFoundException;
import com.warehouse.outbound.repository.OutboundOrderQueryRepository;
import com.warehouse.outbound.repository.OutboundOrderRepository;
import com.warehouse.outbound.service.dto.OutboundOrderCreateRequest;
import com.warehouse.outbound.service.dto.OutboundOrderResponse;
import com.warehouse.picking.service.PickingService;
import com.warehouse.picking.service.dto.PickingTaskCreateCommand;
import com.warehouse.picking.service.dto.PickingWaveCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderQueryRepository outboundOrderQueryRepository;
    private final InventoryService inventoryService;
    private final PickingService pickingService;

    @Transactional
    public OutboundOrderResponse createOutboundOrder(OutboundOrderCreateRequest request) {
        OutboundOrder order = OutboundOrder.create(request.warehouseId(), request.orderId());
        request.items().forEach(item -> order.addItem(item.skuId(), item.requestedQty(), item.locationCode()));
        return OutboundOrderResponse.from(outboundOrderRepository.save(order));
    }

    public OutboundOrderResponse getOutboundOrder(Long id) {
        return OutboundOrderResponse.from(getOrderWithItems(id));
    }

    @Transactional
    public OutboundOrderResponse allocateOutboundOrder(Long id) {
        OutboundOrder order = getOrderWithItems(id);
        if (order.getStatus() != OutboundStatus.PENDING) {
            throw new InvalidStatusException();
        }
        order.getItems().forEach(item -> inventoryService.allocateStock(
            order.getWarehouseId(),
            item.getSkuId(),
            item.getRequestedQty(),
            order.getId()
        ));
        order.allocate();
        pickingService.createPickingWave(new PickingWaveCreateCommand(
            order.getWarehouseId(),
            order.getId(),
            order.getItems().stream()
                .map(item -> new PickingTaskCreateCommand(
                    item.getId(),
                    item.getSkuId(),
                    item.getLocationCode(),
                    item.getRequestedQty()
                ))
                .toList()
        ));
        return OutboundOrderResponse.from(order);
    }

    @Transactional
    public OutboundOrderResponse shipOutboundOrder(Long id) {
        OutboundOrder order = getOrderWithItems(id);
        if (order.getStatus() != OutboundStatus.ALLOCATED) {
            throw new InvalidStatusException();
        }
        order.getItems().forEach(item -> {
            inventoryService.shipStock(order.getWarehouseId(), item.getSkuId(), item.getRequestedQty(), order.getId());
            item.ship();
        });
        order.ship();
        return OutboundOrderResponse.from(order);
    }

    @Transactional
    public OutboundOrderResponse cancelOutboundOrder(Long id) {
        OutboundOrder order = getOrderWithItems(id);
        if (order.getStatus() == OutboundStatus.ALLOCATED) {
            order.getItems().forEach(item -> inventoryService.releaseStock(
                order.getWarehouseId(),
                item.getSkuId(),
                item.getRequestedQty(),
                order.getId()
            ));
        }
        order.cancel();
        return OutboundOrderResponse.from(order);
    }

    private OutboundOrder getOrderWithItems(Long id) {
        return outboundOrderQueryRepository.findByIdWithItems(id)
            .orElseThrow(OutboundOrderNotFoundException::new);
    }
}
