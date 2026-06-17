package com.warehouse.picking.repository;

import com.warehouse.picking.domain.PickingTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickingTaskRepository extends JpaRepository<PickingTask, Long> {

    List<PickingTask> findByWaveId(Long waveId);
}
