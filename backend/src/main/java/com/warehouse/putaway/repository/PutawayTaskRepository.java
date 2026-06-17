package com.warehouse.putaway.repository;

import com.warehouse.putaway.domain.PutawayStatus;
import com.warehouse.putaway.domain.PutawayTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long> {

    List<PutawayTask> findByWarehouseIdAndStatus(Long warehouseId, PutawayStatus status);
}
