package com.warehouse.inventory.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.warehouse.common.exception.InvalidInputException;
import com.warehouse.inventory.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    void release_throwsWhenAllocatedQtyIsNotEnough() {
        Inventory inventory = Inventory.create(1L, 100L, "상품A", 10);

        assertThatThrownBy(() -> inventory.release(1))
            .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void adjust_throwsWhenAdjustmentMakesAvailableQtyNegative() {
        Inventory inventory = Inventory.create(1L, 100L, "상품A", 3);

        assertThatThrownBy(() -> inventory.adjust(-4))
            .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void increase_throwsWhenQtyIsNotPositive() {
        Inventory inventory = Inventory.create(1L, 100L, "상품A", 3);

        assertThatThrownBy(() -> inventory.increase(0))
            .isInstanceOf(InvalidInputException.class);
    }
}
