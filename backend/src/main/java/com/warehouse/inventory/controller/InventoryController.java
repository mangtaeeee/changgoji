package com.warehouse.inventory.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.InventoryAdjustRequest;
import com.warehouse.inventory.service.dto.InventoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @GetMapping("/{skuId}")
    @Operation(summary = "SKU별 재고 조회", description = "창고 ID와 SKU ID로 가용 재고와 할당 재고를 조회합니다.")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable Long skuId, @RequestParam Long warehouseId) {
        return ApiResponse.success(inventoryService.getInventory(warehouseId, skuId));
    }

    @PostMapping("/adjust")
    @Operation(summary = "재고 조정", description = "실사 결과나 파손 등 운영 사유로 가용 재고를 수동 조정합니다.")
    public ApiResponse<InventoryResponse> adjustInventory(@Valid @RequestBody InventoryAdjustRequest request) {
        return ApiResponse.success(inventoryService.adjustInventory(request));
    }
}
