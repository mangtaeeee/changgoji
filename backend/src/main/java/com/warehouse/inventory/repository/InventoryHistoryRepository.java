package com.warehouse.inventory.repository;

import com.warehouse.inventory.domain.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {
}
