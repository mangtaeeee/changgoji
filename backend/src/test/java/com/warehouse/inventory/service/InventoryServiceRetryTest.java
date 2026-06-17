package com.warehouse.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.inventory.domain.Inventory;
import com.warehouse.inventory.domain.InventoryHistory;
import com.warehouse.inventory.repository.InventoryHistoryRepository;
import com.warehouse.inventory.repository.InventoryLocationRepository;
import com.warehouse.inventory.repository.InventoryQueryRepository;
import com.warehouse.inventory.repository.InventoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {
    InventoryService.class,
    InventoryServiceRetryTest.RetryTestConfig.class
})
class InventoryServiceRetryTest {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    @Autowired
    InventoryServiceRetryTest(
        InventoryService inventoryService,
        InventoryRepository inventoryRepository,
        InventoryHistoryRepository inventoryHistoryRepository
    ) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
    }

    @BeforeEach
    void setUp() {
        reset(inventoryRepository, inventoryHistoryRepository);
    }

    @Test
    void allocateStock_retriesOptimisticLockConflictAndSucceedsOnThirdAttempt() {
        // 각 재시도는 새로운 트랜잭션에서 재고를 다시 읽는 상황을 흉내낸다.
        Inventory firstAttemptInventory = Inventory.create(1L, 100L, "상품A", 10);
        Inventory secondAttemptInventory = Inventory.create(1L, 100L, "상품A", 10);
        Inventory thirdAttemptInventory = Inventory.create(1L, 100L, "상품A", 10);
        when(inventoryRepository.findByWarehouseIdAndSkuId(1L, 100L))
            .thenReturn(Optional.of(firstAttemptInventory))
            .thenReturn(Optional.of(secondAttemptInventory))
            .thenReturn(Optional.of(thirdAttemptInventory));

        // 재고 이력 저장 시 낙관적 락 충돌이 2번 발생하고, 3번째 시도에서 성공하도록 만든다.
        when(inventoryHistoryRepository.save(any(InventoryHistory.class)))
            .thenThrow(new OptimisticLockingFailureException("first conflict"))
            .thenThrow(new OptimisticLockingFailureException("second conflict"))
            .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.allocateStock(1L, 100L, 3, 900L);

        verify(inventoryRepository, times(3)).findByWarehouseIdAndSkuId(1L, 100L);
        verify(inventoryHistoryRepository, times(3)).save(any(InventoryHistory.class));
        assertThat(thirdAttemptInventory.getAvailableQty()).isEqualTo(7);
        assertThat(thirdAttemptInventory.getAllocatedQty()).isEqualTo(3);
    }

    @EnableRetry
    @Configuration
    static class RetryTestConfig {

        @Bean
        InventoryRepository inventoryRepository() {
            return mock(InventoryRepository.class);
        }

        @Bean
        InventoryHistoryRepository inventoryHistoryRepository() {
            return mock(InventoryHistoryRepository.class);
        }

        @Bean
        InventoryLocationRepository inventoryLocationRepository() {
            return mock(InventoryLocationRepository.class);
        }

        @Bean
        InventoryQueryRepository inventoryQueryRepository() {
            return mock(InventoryQueryRepository.class);
        }
    }
}
