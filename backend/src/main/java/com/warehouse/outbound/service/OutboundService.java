package com.warehouse.outbound.service;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.outbound.domain.OutboundOrder;
import com.warehouse.outbound.domain.OutboundStatus;
import com.warehouse.outbound.repository.OutboundOrderQueryRepository;
import com.warehouse.outbound.repository.OutboundOrderRepository;
import com.warehouse.outbound.service.dto.OutboundOrderCreateRequest;
import com.warehouse.outbound.service.dto.OutboundOrderResponse;
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

    @Transactional
    public OutboundOrderResponse createOutboundOrder(OutboundOrderCreateRequest request) {
        OutboundOrder order = OutboundOrder.create(request.warehouseId(), request.orderId());
        request.items().forEach(item -> order.addItem(item.skuId(), item.requestedQty(), item.locationCode()));
        return OutboundOrderResponse.from(outboundOrderRepository.save(order));
    }

    public OutboundOrderResponse getOutboundOrder(Long id) {
        return OutboundOrderResponse.from(getOrder(id));
    }

    @Transactional
    public OutboundOrderResponse allocateOutboundOrder(Long id) {
        OutboundOrder order = getOrderWithItems(id);
        order.getItems().forEach(item -> inventoryService.allocateStock(
            order.getWarehouseId(),
            item.getSkuId(),
            item.getRequestedQty(),
            order.getId()
        ));
        order.allocate();
        return OutboundOrderResponse.from(order);
    }

    @Transactional
    public OutboundOrderResponse shipOutboundOrder(Long id) {
        OutboundOrder order = getOrderWithItems(id);
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

    private OutboundOrder getOrder(Long id) {
        return outboundOrderRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOUND_ORDER_NOT_FOUND));
    }

    private OutboundOrder getOrderWithItems(Long id) {
        return outboundOrderQueryRepository.findByIdWithItems(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOUND_ORDER_NOT_FOUND));
    }
}
