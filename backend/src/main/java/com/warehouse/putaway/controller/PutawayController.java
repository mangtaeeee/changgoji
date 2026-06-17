package com.warehouse.putaway.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.putaway.domain.PutawayStatus;
import com.warehouse.putaway.service.PutawayService;
import com.warehouse.putaway.service.dto.PutawayConfirmRequest;
import com.warehouse.putaway.service.dto.PutawayStartRequest;
import com.warehouse.putaway.service.dto.PutawayTaskResponse;
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
@RequestMapping("/api/v1/putaway-tasks")
@Tag(name = "적치", description = "입고 확정 후 생성되는 적치 작업 조회, 시작, 확정 API")
public class PutawayController {

    private final PutawayService putawayService;

    @GetMapping
    @Operation(summary = "적치 작업 목록 조회", description = "창고와 작업 상태 기준으로 적치 작업 목록을 조회합니다.")
    public ApiResponse<List<PutawayTaskResponse>> getPutawayTasks(@RequestParam Long warehouseId,
        @RequestParam(defaultValue = "PENDING") PutawayStatus status) {
        return ApiResponse.success(putawayService.getPutawayTasks(warehouseId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "적치 작업 단건 조회", description = "적치 작업 ID로 추천 위치와 작업 상태를 조회합니다.")
    public ApiResponse<PutawayTaskResponse> getPutawayTask(@PathVariable Long id) {
        return ApiResponse.success(putawayService.getPutawayTask(id));
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "적치 작업 시작", description = "작업자를 배정하고 적치 작업을 진행 중 상태로 변경합니다.")
    public ApiResponse<PutawayTaskResponse> startPutawayTask(@PathVariable Long id,
        @Valid @RequestBody PutawayStartRequest request) {
        return ApiResponse.success(putawayService.startPutawayTask(id, request));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "적치 확정", description = "확정 위치를 반영하고 위치별 재고 수량을 증가시킵니다.")
    public ApiResponse<PutawayTaskResponse> confirmPutawayTask(@PathVariable Long id,
        @Valid @RequestBody PutawayConfirmRequest request) {
        return ApiResponse.success(putawayService.confirmPutawayTask(id, request));
    }
}
