package com.warehouse.returns.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.inventory.domain.ChangeType;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.StockIncreaseCommand;
import com.warehouse.returns.domain.ItemCondition;
import com.warehouse.returns.domain.ReturnOrder;
import com.warehouse.returns.domain.ReturnReason;
import com.warehouse.returns.repository.ReturnOrderQueryRepository;
import com.warehouse.returns.repository.ReturnOrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock
    private ReturnOrderRepository returnOrderRepository;

    @Mock
    private ReturnOrderQueryRepository returnOrderQueryRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ReturnService returnService;

    @Test
    void completeReturnOrder_restoresStockWhenItemIsResellable() {
        // 반품 완료 시 재판매 가능한 상품만 가용 재고로 복구해야 한다.
        // Given: 재판매 가능 상태로 2개가 회수된 반품 지시
        ReturnOrder order = ReturnOrder.create(1L, 1L, ReturnReason.CUSTOMER_CHANGE);
        ReflectionTestUtils.setField(order, "id", 1L);
        order.addItem(100L, "상품A", 2);
        ReflectionTestUtils.setField(order.getItems().get(0), "id", 31L);
        order.getItems().get(0).receive(2, ItemCondition.RESELLABLE);

        when(returnOrderQueryRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        // When: 반품을 완료한다.
        returnService.completeReturnOrder(1L);

        // Then: 재고 복구는 수행되고, 불량 이력 기록은 수행되지 않는다.
        verify(inventoryService).increaseStock(any(StockIncreaseCommand.class), any(ChangeType.class));
        verify(inventoryService, never()).recordDefectiveReturn(any(), any(), any());
    }

    @Test
    void completeReturnOrder_recordsDefectiveHistoryWithoutRestoringStock() {
        // 불량 반품은 판매 가능 재고로 복구하지 않고, 별도 이력만 남겨야 한다.
        // Given: 불량 상태로 1개가 회수된 반품 지시
        ReturnOrder order = ReturnOrder.create(1L, 1L, ReturnReason.DEFECT);
        ReflectionTestUtils.setField(order, "id", 2L);
        order.addItem(100L, "상품A", 1);
        ReflectionTestUtils.setField(order.getItems().get(0), "id", 32L);
        order.getItems().get(0).receive(1, ItemCondition.DEFECTIVE);

        when(returnOrderQueryRepository.findByIdWithItems(2L)).thenReturn(Optional.of(order));

        // When: 반품을 완료한다.
        returnService.completeReturnOrder(2L);

        // Then: 재고 복구는 하지 않고 DEFECTIVE_RETURN 이력 기록만 요청한다.
        verify(inventoryService, never()).increaseStock(any(), any());
        verify(inventoryService).recordDefectiveReturn(1L, 100L, 2L);
    }
}
