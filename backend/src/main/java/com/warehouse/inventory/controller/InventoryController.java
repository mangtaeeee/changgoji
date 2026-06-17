package com.warehouse.inventory.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.inventory.service.InventoryService;
import com.warehouse.inventory.service.dto.InventoryAdjustRequest;
import com.warehouse.inventory.service.dto.InventoryResponse;
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
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{skuId}")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable Long skuId, @RequestParam Long warehouseId) {
        return ApiResponse.success(inventoryService.getInventory(warehouseId, skuId));
    }

    @PostMapping("/adjust")
    public ApiResponse<InventoryResponse> adjustInventory(@Valid @RequestBody InventoryAdjustRequest request) {
        return ApiResponse.success(inventoryService.adjustInventory(request));
    }
}
