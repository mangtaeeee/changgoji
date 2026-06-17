package com.warehouse.inbound.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.inbound.service.InboundService;
import com.warehouse.inbound.service.dto.InboundConfirmRequest;
import com.warehouse.inbound.service.dto.InboundOrderCreateRequest;
import com.warehouse.inbound.service.dto.InboundOrderResponse;
import com.warehouse.inbound.service.dto.InboundReceiveRequest;
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
@RequestMapping("/api/v1/inbound-orders")
public class InboundController {

    private final InboundService inboundService;

    @PostMapping
    public ApiResponse<InboundOrderResponse> createInboundOrder(@Valid @RequestBody InboundOrderCreateRequest request) {
        return ApiResponse.success(inboundService.createInboundOrder(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InboundOrderResponse> getInboundOrder(@PathVariable Long id) {
        return ApiResponse.success(inboundService.getInboundOrder(id));
    }

    @PatchMapping("/{id}/receive")
    public ApiResponse<InboundOrderResponse> receiveInboundOrder(@PathVariable Long id,
        @Valid @RequestBody InboundReceiveRequest request) {
        return ApiResponse.success(inboundService.receiveInboundOrder(id, request));
    }

    @PatchMapping("/{id}/confirm")
    public ApiResponse<InboundOrderResponse> confirmInboundOrder(@PathVariable Long id,
        @Valid @RequestBody InboundConfirmRequest request) {
        return ApiResponse.success(inboundService.confirmInboundOrder(id, request));
    }
}
