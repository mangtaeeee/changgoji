package com.warehouse.returns.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.returns.service.ReturnService;
import com.warehouse.returns.service.dto.ReturnOrderCreateRequest;
import com.warehouse.returns.service.dto.ReturnOrderResponse;
import com.warehouse.returns.service.dto.ReturnReceiveRequest;
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
@RequestMapping("/api/v1/return-orders")
@Tag(name = "반품", description = "반품 접수, 반품 실사, 반품 완료 및 거부 API")
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    @Operation(summary = "반품 접수", description = "출고 지시를 기준으로 반품 요청 품목과 사유를 등록합니다.")
    public ApiResponse<ReturnOrderResponse> createReturnOrder(@Valid @RequestBody ReturnOrderCreateRequest request) {
        return ApiResponse.success(returnService.createReturnOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "반품 단건 조회", description = "반품 지시 ID로 반품 상태와 반품 사유를 조회합니다.")
    public ApiResponse<ReturnOrderResponse> getReturnOrder(@PathVariable Long id) {
        return ApiResponse.success(returnService.getReturnOrder(id));
    }

    @PatchMapping("/{id}/receive")
    @Operation(summary = "반품 실사 처리", description = "반품 품목별 실제 회수 수량과 상품 상태를 반영합니다.")
    public ApiResponse<ReturnOrderResponse> receiveReturnOrder(@PathVariable Long id,
        @Valid @RequestBody ReturnReceiveRequest request) {
        return ApiResponse.success(returnService.receiveReturnOrder(id, request));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "반품 완료", description = "재판매 가능 상품은 재고로 복구하고 불량 상품은 불량 이력만 기록합니다.")
    public ApiResponse<ReturnOrderResponse> completeReturnOrder(@PathVariable Long id) {
        return ApiResponse.success(returnService.completeReturnOrder(id));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "반품 거부", description = "검수 결과 반품 처리가 불가한 지시를 거부 상태로 변경합니다.")
    public ApiResponse<ReturnOrderResponse> rejectReturnOrder(@PathVariable Long id) {
        return ApiResponse.success(returnService.rejectReturnOrder(id));
    }
}
