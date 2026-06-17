package com.warehouse.outbound.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.outbound.service.OutboundService;
import com.warehouse.outbound.service.dto.OutboundOrderCreateRequest;
import com.warehouse.outbound.service.dto.OutboundOrderResponse;
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
@RequestMapping("/api/v1/outbound-orders")
@Tag(name = "출고", description = "출고 지시 등록, 재고 할당, 출고 확정, 출고 취소 API")
public class OutboundController {

    private final OutboundService outboundService;

    @PostMapping
    @Operation(summary = "출고 지시 등록", description = "외부 주문 ID와 출고 품목을 기준으로 출고 지시를 생성합니다.")
    public ApiResponse<OutboundOrderResponse> createOutboundOrder(@Valid @RequestBody OutboundOrderCreateRequest request) {
        return ApiResponse.success(outboundService.createOutboundOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "출고 지시 단건 조회", description = "출고 지시 ID로 출고 상태와 외부 주문 ID를 조회합니다.")
    public ApiResponse<OutboundOrderResponse> getOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.getOutboundOrder(id));
    }

    @PatchMapping("/{id}/allocate")
    @Operation(summary = "재고 할당", description = "출고 요청 수량만큼 가용 재고를 차감하고 할당 재고로 이동합니다.")
    public ApiResponse<OutboundOrderResponse> allocateOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.allocateOutboundOrder(id));
    }

    @PatchMapping("/{id}/ship")
    @Operation(summary = "출고 확정", description = "할당된 재고를 최종 출고 처리하고 출고 수량을 확정합니다.")
    public ApiResponse<OutboundOrderResponse> shipOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.shipOutboundOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "출고 취소", description = "출고 지시를 취소하고 할당된 재고가 있으면 가용 재고로 복구합니다.")
    public ApiResponse<OutboundOrderResponse> cancelOutboundOrder(@PathVariable Long id) {
        return ApiResponse.success(outboundService.cancelOutboundOrder(id));
    }
}
