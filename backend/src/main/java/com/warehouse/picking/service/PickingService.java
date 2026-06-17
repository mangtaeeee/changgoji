package com.warehouse.picking.service;

import com.warehouse.common.exception.InvalidStatusException;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.StockLocationDecreaseCommand;
import com.warehouse.picking.domain.PickingTask;
import com.warehouse.picking.domain.PickingTaskStatus;
import com.warehouse.picking.domain.PickingWave;
import com.warehouse.picking.domain.PickingWaveStatus;
import com.warehouse.picking.exception.PickingTaskNotFoundException;
import com.warehouse.picking.exception.PickingWaveNotFoundException;
import com.warehouse.picking.repository.PickingTaskRepository;
import com.warehouse.picking.repository.PickingWaveRepository;
import com.warehouse.picking.service.dto.PickingTaskPickRequest;
import com.warehouse.picking.service.dto.PickingTaskResponse;
import com.warehouse.picking.service.dto.PickingWaveCreateCommand;
import com.warehouse.picking.service.dto.PickingWaveResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PickingService {

    private final PickingWaveRepository pickingWaveRepository;
    private final PickingTaskRepository pickingTaskRepository;
    private final InventoryService inventoryService;

    @Transactional
    public PickingWaveResponse createPickingWave(PickingWaveCreateCommand command) {
        PickingWave wave = PickingWave.create(command.warehouseId(), command.outboundOrderId());
        command.tasks().forEach(task -> wave.addTask(
            task.outboundItemId(),
            task.skuId(),
            task.locationCode(),
            task.qty()
        ));
        return PickingWaveResponse.from(pickingWaveRepository.save(wave));
    }

    public List<PickingWaveResponse> getPickingWaves(Long warehouseId, PickingWaveStatus status) {
        return pickingWaveRepository.findByWarehouseIdAndStatus(warehouseId, status).stream()
            .map(PickingWaveResponse::from)
            .toList();
    }

    public PickingWaveResponse getPickingWave(Long id) {
        return PickingWaveResponse.from(getWave(id));
    }

    public List<PickingTaskResponse> getPickingTasks(Long waveId) {
        return pickingTaskRepository.findByWaveId(waveId).stream()
            .map(PickingTaskResponse::from)
            .toList();
    }

    @Transactional
    public PickingTaskResponse pickTask(Long id, PickingTaskPickRequest request) {
        PickingTask task = getTask(id);
        if (task.getStatus() != PickingTaskStatus.PENDING) {
            throw new InvalidStatusException();
        }
        task.getWave().start();
        inventoryService.decreaseLocationStock(new StockLocationDecreaseCommand(
            task.getWave().getWarehouseId(),
            task.getSkuId(),
            task.getLocationCode(),
            task.getQty()
        ));
        task.pick(request.assignedTo());
        completeWaveIfAllTasksFinished(task.getWave());
        return PickingTaskResponse.from(task);
    }

    private void completeWaveIfAllTasksFinished(PickingWave wave) {
        boolean allFinished = wave.getTasks().stream()
            .allMatch(task -> task.getStatus() != PickingTaskStatus.PENDING);
        if (allFinished) {
            wave.complete();
        }
    }

    private PickingWave getWave(Long id) {
        return pickingWaveRepository.findById(id)
            .orElseThrow(PickingWaveNotFoundException::new);
    }

    private PickingTask getTask(Long id) {
        return pickingTaskRepository.findById(id)
            .orElseThrow(PickingTaskNotFoundException::new);
    }
}
