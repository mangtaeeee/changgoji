package com.warehouse.picking.repository;

import com.warehouse.picking.domain.PickingWave;
import com.warehouse.picking.domain.PickingWaveStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickingWaveRepository extends JpaRepository<PickingWave, Long> {

    List<PickingWave> findByWarehouseIdAndStatus(Long warehouseId, PickingWaveStatus status);
}
