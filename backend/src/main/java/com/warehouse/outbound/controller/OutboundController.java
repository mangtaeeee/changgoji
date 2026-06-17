package com.warehouse.outbound.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.outbound.service.OutboundService;
import com.warehouse.outbound.service.dto.OutboundOrderCreateRequest;
import com.warehouse.outbound.service.dto.OutboundOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/outbound-orders")
public class OutboundController {

    private final OutboundService outboundService;

    @PostMapping
    public ApiResponse<OutboundOrderResponse> createOutboundOrder(@Valid @RequestBody OutboundOrderCreateRequest request) {
        return ApiResponse.success(outboundService.createOutboundOrder(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OutboundOrderResponse> getOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.getOutboundOrder(id));
    }

    @PatchMapping("/{id}/allocate")
    public ApiResponse<OutboundOrderResponse> allocateOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.allocateOutboundOrder(id));
    }

    @PatchMapping("/{id}/ship")
    public ApiResponse<OutboundOrderResponse> shipOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.shipOutboundOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OutboundOrderResponse> cancelOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.cancelOutboundOrder(id));
    }
}
