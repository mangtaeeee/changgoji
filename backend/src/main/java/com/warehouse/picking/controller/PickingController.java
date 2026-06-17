package com.warehouse.picking.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.picking.domain.PickingWaveStatus;
import com.warehouse.picking.service.PickingService;
import com.warehouse.picking.service.dto.PickingTaskPickRequest;
import com.warehouse.picking.service.dto.PickingTaskResponse;
import com.warehouse.picking.service.dto.PickingWaveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "피킹", description = "출고 할당 후 생성되는 피킹 웨이브와 피킹 작업 처리 API")
public class PickingController {

    private final PickingService pickingService;

    @GetMapping("/picking-waves")
    @Operation(summary = "피킹 웨이브 목록 조회", description = "창고와 상태 기준으로 피킹 웨이브 목록을 조회합니다.")
    public ApiResponse<List<PickingWaveResponse>> getPickingWaves(@RequestParam Long warehouseId,
        @RequestParam(defaultValue = "OPEN") PickingWaveStatus status) {
        return ApiResponse.success(pickingService.getPickingWaves(warehouseId, status));
    }

    @GetMapping("/picking-waves/{id}")
    @Operation(summary = "피킹 웨이브 단건 조회", description = "피킹 웨이브 ID로 작업 목록과 진행 상태를 조회합니다.")
    public ApiResponse<PickingWaveResponse> getPickingWave(@PathVariable Long id) {
        return ApiResponse.success(pickingService.getPickingWave(id));
    }

    @GetMapping("/picking-waves/{id}/tasks")
    @Operation(summary = "피킹 작업 목록 조회", description = "피킹 웨이브에 포함된 위치별 피킹 작업 목록을 조회합니다.")
    public ApiResponse<List<PickingTaskResponse>> getPickingTasks(@PathVariable Long id) {
        return ApiResponse.success(pickingService.getPickingTasks(id));
    }

    @PatchMapping("/picking-tasks/{id}/pick")
    @Operation(summary = "피킹 완료", description = "작업자가 위치에서 상품을 집고 위치별 재고 수량을 차감합니다.")
    public ApiResponse<PickingTaskResponse> pickTask(@PathVariable Long id,
        @Valid @RequestBody PickingTaskPickRequest request) {
        return ApiResponse.success(pickingService.pickTask(id, request));
    }
}
