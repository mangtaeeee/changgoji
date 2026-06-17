package com.warehouse.inventory.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.InventoryAdjustRequest;
import com.warehouse.inventory.service.dto.InventoryHistoryResponse;
import com.warehouse.inventory.service.dto.InventoryListResponse;
import com.warehouse.inventory.service.dto.InventoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventories")
@Tag(name = "재고", description = "창고 재고 조회, 실사 조정, 재고 이력 기반 관리 API")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "창고 재고 목록 조회", description = "창고 ID 기준으로 SKU별 재고와 위치별 수량을 조회합니다. QueryDSL DTO Projection을 사용합니다.")
    public ApiResponse<List<InventoryListResponse>> getInventories(@RequestParam Long warehouseId) {
        return ApiResponse.success(inventoryService.getInventories(warehouseId));
    }

    @GetMapping("/{skuId}")
    @Operation(summary = "SKU별 재고 조회", description = "창고 ID와 SKU ID로 가용 재고와 할당 재고를 조회합니다.")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable Long skuId, @RequestParam Long warehouseId) {
        return ApiResponse.success(inventoryService.getInventory(warehouseId, skuId));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "재고 이력 조회", description = "재고 ID 기준으로 입고, 할당, 출고, 반품, 조정 이력을 최신순으로 조회합니다.")
    public ApiResponse<List<InventoryHistoryResponse>> getInventoryHistories(@PathVariable Long id) {
        return ApiResponse.success(inventoryService.getInventoryHistories(id));
    }

    @PostMapping("/adjust")
    @Operation(summary = "재고 조정", description = "실사 결과나 파손 등 운영 사유로 가용 재고를 수동 조정합니다.")
    public ApiResponse<InventoryResponse> adjustInventory(@Valid @RequestBody InventoryAdjustRequest request) {
        return ApiResponse.success(inventoryService.adjustInventory(request));
    }
}
