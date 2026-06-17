package com.warehouse.inbound.service;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;
import com.warehouse.inbound.domain.InboundOrder;
import com.warehouse.inbound.domain.InboundReceipt;
import com.warehouse.inbound.repository.InboundOrderRepository;
import com.warehouse.inbound.repository.InboundReceiptRepository;
import com.warehouse.inbound.service.dto.InboundConfirmRequest;
import com.warehouse.inbound.service.dto.InboundOrderCreateRequest;
import com.warehouse.inbound.service.dto.InboundOrderResponse;
import com.warehouse.inbound.service.dto.InboundReceiveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InboundService {

    private final InboundOrderRepository inboundOrderRepository;
    private final InboundReceiptRepository inboundReceiptRepository;

    @Transactional
    public InboundOrderResponse createInboundOrder(InboundOrderCreateRequest request) {
        InboundOrder order = InboundOrder.create(request.warehouseId(), request.supplierId(), request.scheduledDate());
        request.items().forEach(item -> order.addItem(item.skuId(), item.skuName(), item.orderedQty()));
        return InboundOrderResponse.from(inboundOrderRepository.save(order));
    }

    public InboundOrderResponse getInboundOrder(Long id) {
        return InboundOrderResponse.from(getOrder(id));
    }

    @Transactional
    public InboundOrderResponse receiveInboundOrder(Long id, InboundReceiveRequest request) {
        InboundOrder order = getOrder(id);
        order.startReceiving();
        request.items().forEach(received -> order.getItems().stream()
            .filter(item -> item.getId().equals(received.inboundItemId()))
            .findFirst()
            .ifPresent(item -> item.receive(received.receivedQty())));
        return InboundOrderResponse.from(order);
    }

    @Transactional
    public InboundOrderResponse confirmInboundOrder(Long id, InboundConfirmRequest request) {
        InboundOrder order = getOrder(id);
        order.confirm();
        inboundReceiptRepository.save(InboundReceipt.create(order, request.confirmedBy(), request.memo()));
        return InboundOrderResponse.from(order);
    }

    private InboundOrder getOrder(Long id) {
        return inboundOrderRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.INBOUND_ORDER_NOT_FOUND));
    }
}
