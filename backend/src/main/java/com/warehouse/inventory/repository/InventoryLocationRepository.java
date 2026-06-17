package com.warehouse.inventory.repository;

import com.warehouse.inventory.domain.InventoryLocation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {

    Optional<InventoryLocation> findByInventoryIdAndLocationCode(Long inventoryId, String locationCode);
}
