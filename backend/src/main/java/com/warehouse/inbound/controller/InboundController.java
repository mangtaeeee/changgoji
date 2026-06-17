package com.warehouse.inbound.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.inbound.service.InboundService;
import com.warehouse.inbound.service.dto.InboundConfirmRequest;
import com.warehouse.inbound.service.dto.InboundOrderCreateRequest;
import com.warehouse.inbound.service.dto.InboundOrderResponse;
import com.warehouse.inbound.service.dto.InboundReceiveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "입고", description = "입고 지시 등록, 실사 입고, 입고 확정 API")
public class InboundController {

    private final InboundService inboundService;

    @PostMapping
    @Operation(summary = "입고 지시 등록", description = "창고와 공급사 기준으로 입고 예정 품목을 등록합니다.")
    public ApiResponse<InboundOrderResponse> createInboundOrder(@Valid @RequestBody InboundOrderCreateRequest request) {
        return ApiResponse.success(inboundService.createInboundOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "입고 지시 단건 조회", description = "입고 지시 ID로 입고 상태와 예정일을 조회합니다.")
    public ApiResponse<InboundOrderResponse> getInboundOrder(@PathVariable Long id) {
        return ApiResponse.success(inboundService.getInboundOrder(id));
    }

    @PatchMapping("/{id}/receive")
    @Operation(summary = "실사 입고 처리", description = "현장에서 확인한 실제 입고 수량을 입고 품목별로 반영합니다.")
    public ApiResponse<InboundOrderResponse> receiveInboundOrder(@PathVariable Long id,
        @Valid @RequestBody InboundReceiveRequest request) {
        return ApiResponse.success(inboundService.receiveInboundOrder(id, request));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "입고 확정", description = "입고 지시를 확정하고 실사 수량 기준으로 재고를 증가시킵니다.")
    public ApiResponse<InboundOrderResponse> confirmInboundOrder(@PathVariable Long id,
        @Valid @RequestBody InboundConfirmRequest request) {
        return ApiResponse.success(inboundService.confirmInboundOrder(id, request));
    }
}
