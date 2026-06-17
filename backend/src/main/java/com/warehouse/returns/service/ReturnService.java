package com.warehouse.returns.service;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;
import com.warehouse.inventory.domain.ChangeType;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.StockIncreaseCommand;
import com.warehouse.returns.domain.ItemCondition;
import com.warehouse.returns.domain.ReturnItem;
import com.warehouse.returns.domain.ReturnOrder;
import com.warehouse.returns.repository.ReturnOrderQueryRepository;
import com.warehouse.returns.repository.ReturnOrderRepository;
import com.warehouse.returns.service.dto.ReturnOrderCreateRequest;
import com.warehouse.returns.service.dto.ReturnOrderResponse;
import com.warehouse.returns.service.dto.ReturnReceiveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnOrderRepository returnOrderRepository;
    private final ReturnOrderQueryRepository returnOrderQueryRepository;
    private final InventoryService inventoryService;

    @Transactional
    public ReturnOrderResponse createReturnOrder(ReturnOrderCreateRequest request) {
        ReturnOrder order = ReturnOrder.create(request.outboundOrderId(), request.warehouseId(), request.reason());
        request.items().forEach(item -> order.addItem(item.skuId(), item.skuName(), item.requestedQty()));
        return ReturnOrderResponse.from(returnOrderRepository.save(order));
    }

    public ReturnOrderResponse getReturnOrder(Long id) {
        return ReturnOrderResponse.from(getOrderWithItems(id));
    }

    @Transactional
    public ReturnOrderResponse receiveReturnOrder(Long id, ReturnReceiveRequest request) {
        ReturnOrder order = getOrderWithItems(id);
        order.receive();
        request.items().forEach(received -> findItem(order, received.returnItemId())
            .receive(received.receivedQty(), received.condition()));
        return ReturnOrderResponse.from(order);
    }

    @Transactional
    public ReturnOrderResponse completeReturnOrder(Long id) {
        ReturnOrder order = getOrderWithItems(id);
        order.getItems().stream()
            .filter(item -> item.getReceivedQty() > 0)
            .forEach(item -> restoreOrRecordDefective(order, item));
        order.complete();
        return ReturnOrderResponse.from(order);
    }

    @Transactional
    public ReturnOrderResponse rejectReturnOrder(Long id) {
        ReturnOrder order = getOrderWithItems(id);
        order.reject();
        return ReturnOrderResponse.from(order);
    }

    private void restoreOrRecordDefective(ReturnOrder order, ReturnItem item) {
        if (item.getCondition() == ItemCondition.RESELLABLE) {
            inventoryService.increaseStock(
                new StockIncreaseCommand(
                    order.getWarehouseId(),
                    item.getSkuId(),
                    item.getSkuName(),
                    item.getReceivedQty(),
                    order.getId()
                ),
                ChangeType.RETURN_INBOUND
            );
            return;
        }
        inventoryService.recordDefectiveReturn(order.getWarehouseId(), item.getSkuId(), order.getId());
    }

    private ReturnItem findItem(ReturnOrder order, Long returnItemId) {
        return order.getItems().stream()
            .filter(item -> item.getId().equals(returnItemId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
    }

    private ReturnOrder getOrderWithItems(Long id) {
        return returnOrderQueryRepository.findByIdWithItems(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RETURN_ORDER_NOT_FOUND));
    }
}
