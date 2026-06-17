package com.warehouse.outbound.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.inventory.service.InventoryService;
import com.warehouse.outbound.domain.OutboundOrder;
import com.warehouse.outbound.domain.OutboundStatus;
import com.warehouse.outbound.repository.OutboundOrderQueryRepository;
import com.warehouse.outbound.repository.OutboundOrderRepository;
import com.warehouse.picking.service.PickingService;
import com.warehouse.picking.service.dto.PickingWaveCreateCommand;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OutboundServiceTest {

    @Mock
    private OutboundOrderRepository outboundOrderRepository;

    @Mock
    private OutboundOrderQueryRepository outboundOrderQueryRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PickingService pickingService;

    @InjectMocks
    private OutboundService outboundService;

    @Test
    void allocateOutboundOrder_allocatesStockAndCreatesPickingWave() {
        // 출고 할당은 재고를 주문에 묶어두고, 현장 작업자가 수행할 피킹 작업을 만드는 시작점이다.
        // Given: SKU 100을 A-01-03 위치에서 5개 출고해야 하는 출고 지시
        OutboundOrder order = OutboundOrder.create(1L, "ORDER-20260617-001");
        ReflectionTestUtils.setField(order, "id", 1L);
        order.addItem(100L, 5, "A-01-03");
        ReflectionTestUtils.setField(order.getItems().get(0), "id", 21L);

        when(outboundOrderQueryRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        // When: 출고 지시에 재고를 할당한다.
        outboundService.allocateOutboundOrder(1L);

        // Then: 가용 재고를 할당 재고로 이동시키는 명령이 호출된다.
        verify(inventoryService).allocateStock(1L, 100L, 5, 1L);

        // Then: 출고 품목을 기반으로 피킹 웨이브와 피킹 작업이 생성된다.
        ArgumentCaptor<PickingWaveCreateCommand> waveCaptor = ArgumentCaptor.forClass(PickingWaveCreateCommand.class);
        verify(pickingService).createPickingWave(waveCaptor.capture());
        PickingWaveCreateCommand command = waveCaptor.getValue();
        assertThat(command.warehouseId()).isEqualTo(1L);
        assertThat(command.outboundOrderId()).isEqualTo(1L);
        assertThat(command.tasks()).hasSize(1);
        assertThat(command.tasks().get(0).outboundItemId()).isEqualTo(21L);
        assertThat(command.tasks().get(0).locationCode()).isEqualTo("A-01-03");
        // Then: 출고 지시는 할당 완료 상태가 된다.
        assertThat(order.getStatus()).isEqualTo(OutboundStatus.ALLOCATED);
    }
}
