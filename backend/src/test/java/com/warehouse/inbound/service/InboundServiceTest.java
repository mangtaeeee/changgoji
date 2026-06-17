package com.warehouse.inbound.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.inbound.domain.InboundOrder;
import com.warehouse.inbound.domain.InboundStatus;
import com.warehouse.inbound.repository.InboundOrderQueryRepository;
import com.warehouse.inbound.repository.InboundOrderRepository;
import com.warehouse.inbound.repository.InboundReceiptRepository;
import com.warehouse.inbound.service.dto.InboundConfirmRequest;
import com.warehouse.inventory.domain.ChangeType;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.StockIncreaseCommand;
import com.warehouse.putaway.service.PutawayService;
import com.warehouse.putaway.service.dto.PutawayTaskCreateCommand;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @Mock
    private InboundOrderRepository inboundOrderRepository;

    @Mock
    private InboundOrderQueryRepository inboundOrderQueryRepository;

    @Mock
    private InboundReceiptRepository inboundReceiptRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PutawayService putawayService;

    @InjectMocks
    private InboundService inboundService;

    @Test
    void confirmInboundOrder_increasesStockAndCreatesPutawayTask() {
        // 입고 확정은 단순 상태 변경이 아니라, 실사 수량 기준 재고 반영과 적치 작업 생성을 함께 보장해야 한다.
        // Given: 50개 입고 예정 중 48개가 실사 입고된 입고 지시
        InboundOrder order = InboundOrder.create(1L, 10L, LocalDate.of(2026, 6, 20));
        ReflectionTestUtils.setField(order, "id", 1L);
        order.addItem(100L, "상품A", 50);
        ReflectionTestUtils.setField(order.getItems().get(0), "id", 11L);
        order.getItems().get(0).receive(48);

        when(inboundOrderQueryRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        // When: 입고 지시를 확정한다.
        inboundService.confirmInboundOrder(1L, new InboundConfirmRequest(999L, "입고 확정"));

        // Then: 실사 수량 48개가 재고 증가 명령으로 전달된다.
        ArgumentCaptor<StockIncreaseCommand> stockCaptor = ArgumentCaptor.forClass(StockIncreaseCommand.class);
        verify(inventoryService).increaseStock(stockCaptor.capture(), any(ChangeType.class));
        StockIncreaseCommand stockCommand = stockCaptor.getValue();
        assertThat(stockCommand.warehouseId()).isEqualTo(1L);
        assertThat(stockCommand.skuId()).isEqualTo(100L);
        assertThat(stockCommand.qty()).isEqualTo(48);
        assertThat(stockCommand.referenceId()).isEqualTo(1L);

        // Then: 입고 품목 기준으로 적치 작업이 생성된다.
        ArgumentCaptor<PutawayTaskCreateCommand> putawayCaptor = ArgumentCaptor.forClass(PutawayTaskCreateCommand.class);
        verify(putawayService).createPutawayTask(putawayCaptor.capture());
        PutawayTaskCreateCommand putawayCommand = putawayCaptor.getValue();
        assertThat(putawayCommand.inboundOrderId()).isEqualTo(1L);
        assertThat(putawayCommand.inboundItemId()).isEqualTo(11L);
        assertThat(putawayCommand.qty()).isEqualTo(48);
        // Then: 입고 지시는 완료 상태가 되고 입고 확인서가 저장된다.
        assertThat(order.getStatus()).isEqualTo(InboundStatus.COMPLETED);
        verify(inboundReceiptRepository).save(any());
    }
}
