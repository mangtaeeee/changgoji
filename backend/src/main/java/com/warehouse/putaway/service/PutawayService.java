package com.warehouse.putaway.service;

import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.StockLocationCommand;
import com.warehouse.putaway.domain.PutawayStatus;
import com.warehouse.putaway.domain.PutawayTask;
import com.warehouse.putaway.exception.PutawayTaskNotFoundException;
import com.warehouse.putaway.repository.PutawayTaskRepository;
import com.warehouse.putaway.service.dto.PutawayConfirmRequest;
import com.warehouse.putaway.service.dto.PutawayStartRequest;
import com.warehouse.putaway.service.dto.PutawayTaskCreateCommand;
import com.warehouse.putaway.service.dto.PutawayTaskResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PutawayService {

    private final PutawayTaskRepository putawayTaskRepository;
    private final InventoryService inventoryService;

    @Transactional
    public PutawayTaskResponse createPutawayTask(PutawayTaskCreateCommand command) {
        PutawayTask task = PutawayTask.create(
            command.inboundOrderId(),
            command.inboundItemId(),
            command.warehouseId(),
            command.skuId(),
            command.skuName(),
            command.qty(),
            recommendLocation(command.warehouseId(), command.skuId())
        );
        return PutawayTaskResponse.from(putawayTaskRepository.save(task));
    }

    public List<PutawayTaskResponse> getPutawayTasks(Long warehouseId, PutawayStatus status) {
        return putawayTaskRepository.findByWarehouseIdAndStatus(warehouseId, status).stream()
            .map(PutawayTaskResponse::from)
            .toList();
    }

    public PutawayTaskResponse getPutawayTask(Long id) {
        return PutawayTaskResponse.from(getTask(id));
    }

    @Transactional
    public PutawayTaskResponse startPutawayTask(Long id, PutawayStartRequest request) {
        PutawayTask task = getTask(id);
        task.start(request.assignedTo());
        return PutawayTaskResponse.from(task);
    }

    @Transactional
    public PutawayTaskResponse confirmPutawayTask(Long id, PutawayConfirmRequest request) {
        PutawayTask task = getTask(id);
        task.confirm(request.confirmedLocation());
        inventoryService.increaseLocationStock(new StockLocationCommand(
            task.getWarehouseId(),
            task.getSkuId(),
            request.confirmedLocation(),
            task.getQty()
        ));
        return PutawayTaskResponse.from(task);
    }

    private String recommendLocation(Long warehouseId, Long skuId) {
        long zoneNo = (skuId % 3) + 1;
        long rackNo = ((skuId / 3) % 9) + 1;
        return "A-%02d-%02d".formatted(zoneNo, rackNo);
    }

    private PutawayTask getTask(Long id) {
        return putawayTaskRepository.findById(id)
            .orElseThrow(PutawayTaskNotFoundException::new);
    }
}
