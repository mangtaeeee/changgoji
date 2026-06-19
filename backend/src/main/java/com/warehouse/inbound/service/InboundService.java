package com.warehouse.inbound.service;

import com.warehouse.common.exception.InvalidInputException;
import com.warehouse.inbound.domain.InboundOrder;
import com.warehouse.inbound.domain.InboundItem;
import com.warehouse.inbound.domain.InboundReceipt;
import com.warehouse.inbound.exception.InboundOrderNotFoundException;
import com.warehouse.inbound.repository.InboundOrderQueryRepository;
import com.warehouse.inbound.repository.InboundOrderRepository;
import com.warehouse.inbound.repository.InboundReceiptRepository;
import com.warehouse.inbound.service.dto.InboundConfirmRequest;
import com.warehouse.inbound.service.dto.InboundOrderCreateRequest;
import com.warehouse.inbound.service.dto.InboundOrderResponse;
import com.warehouse.inbound.service.dto.InboundReceiveRequest;
import com.warehouse.inventory.domain.ChangeType;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.StockIncreaseCommand;
import java.util.Objects;
import com.warehouse.putaway.service.PutawayService;
import com.warehouse.putaway.service.dto.PutawayTaskCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService {

    private final InboundOrderRepository inboundOrderRepository;
    private final InboundOrderQueryRepository inboundOrderQueryRepository;
    private final InboundReceiptRepository inboundReceiptRepository;
    private final InventoryService inventoryService;
    private final PutawayService putawayService;

    @Transactional
    public InboundOrderResponse createInboundOrder(InboundOrderCreateRequest request) {
        InboundOrder order = InboundOrder.create(request.warehouseId(), request.supplierId(), request.scheduledDate());
        request.items().forEach(item -> order.addItem(item.skuId(), item.skuName(), item.orderedQty()));
        return InboundOrderResponse.from(inboundOrderRepository.save(order));
    }

    public InboundOrderResponse getInboundOrder(Long id) {
        return InboundOrderResponse.from(getOrderWithItems(id));
    }

    @Transactional
    public InboundOrderResponse receiveInboundOrder(Long id, InboundReceiveRequest request) {
        InboundOrder order = getOrderWithItems(id);
        order.startReceiving();
        request.items().forEach(received -> findItem(order, received.inboundItemId()).receive(received.receivedQty()));
        return InboundOrderResponse.from(order);
    }

    @Transactional
    public InboundOrderResponse confirmInboundOrder(Long id, InboundConfirmRequest request) {
        InboundOrder order = getOrderWithItems(id);
        order.confirm();
        order.getItems().stream()
            .filter(item -> item.getReceivedQty() > 0)
            .forEach(item -> {
                inventoryService.increaseStock(
                    new StockIncreaseCommand(
                        order.getWarehouseId(),
                        item.getSkuId(),
                        item.getSkuName(),
                        item.getReceivedQty(),
                        order.getId()
                    ),
                    ChangeType.INBOUND
                );
                putawayService.createPutawayTask(new PutawayTaskCreateCommand(
                    order.getId(),
                    item.getId(),
                    order.getWarehouseId(),
                    item.getSkuId(),
                    item.getSkuName(),
                    item.getReceivedQty()
                ));
            });
        inboundReceiptRepository.save(InboundReceipt.create(order, request.confirmedBy(), request.memo()));
        return InboundOrderResponse.from(order);
    }

    private InboundOrder getOrderWithItems(Long id) {
        return inboundOrderQueryRepository.findByIdWithItems(id)
            .orElseThrow(InboundOrderNotFoundException::new);
    }

    private InboundItem findItem(InboundOrder order, Long inboundItemId) {
        return order.getItems().stream()
            .filter(item -> Objects.equals(item.getId(), inboundItemId))
            .findFirst()
            .orElseThrow(() -> new InvalidInputException("입고 품목을 찾을 수 없습니다."));
    }
}
