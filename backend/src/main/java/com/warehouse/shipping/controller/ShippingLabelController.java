package com.warehouse.shipping.controller;

import com.warehouse.common.response.ApiResponse;
import com.warehouse.shipping.service.ShippingLabelService;
import com.warehouse.shipping.service.dto.ShippingLabelCreateRequest;
import com.warehouse.shipping.service.dto.ShippingLabelFailRequest;
import com.warehouse.shipping.service.dto.ShippingLabelResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipping-labels")
@Tag(name = "송장", description = "송장 생성, 출력 요청, 출력 결과 처리 API")
public class ShippingLabelController {

    private final ShippingLabelService shippingLabelService;

    @PostMapping
    @Operation(summary = "송장 생성", description = "출고 지시 기준으로 운송장 번호와 출력용 송장 데이터를 생성합니다.")
    public ApiResponse<ShippingLabelResponse> createShippingLabel(
        @Valid @RequestBody ShippingLabelCreateRequest request) {
        return ApiResponse.success(shippingLabelService.createShippingLabel(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "송장 단건 조회", description = "송장 ID로 운송장 번호와 출력 상태를 조회합니다.")
    public ApiResponse<ShippingLabelResponse> getShippingLabel(@PathVariable Long id) {
        return ApiResponse.success(shippingLabelService.getShippingLabel(id));
    }

    @GetMapping
    @Operation(summary = "출고 지시별 송장 조회", description = "출고 지시 ID로 생성된 송장을 조회합니다.")
    public ApiResponse<ShippingLabelResponse> getShippingLabelByOutboundOrder(@RequestParam Long outboundOrderId) {
        return ApiResponse.success(shippingLabelService.getShippingLabelByOutboundOrder(outboundOrderId));
    }

    @PostMapping("/{id}/print")
    @Operation(summary = "송장 출력 요청", description = "송장을 출력 요청 상태로 변경해 print-agent가 처리할 수 있게 합니다.")
    public ApiResponse<ShippingLabelResponse> requestPrint(@PathVariable Long id) {
        return ApiResponse.success(shippingLabelService.requestPrint(id));
    }

    @PatchMapping("/{id}/printed")
    @Operation(summary = "송장 출력 성공 처리", description = "print-agent가 출력 성공 결과를 보고하면 송장을 PRINTED 상태로 변경합니다.")
    public ApiResponse<ShippingLabelResponse> markPrinted(@PathVariable Long id) {
        return ApiResponse.success(shippingLabelService.markPrinted(id));
    }

    @PatchMapping("/{id}/failed")
    @Operation(summary = "송장 출력 실패 처리", description = "print-agent가 출력 실패 결과와 사유를 보고하면 FAILED 상태로 변경합니다.")
    public ApiResponse<ShippingLabelResponse> markFailed(@PathVariable Long id,
        @Valid @RequestBody ShippingLabelFailRequest request) {
        return ApiResponse.success(shippingLabelService.markFailed(id, request));
    }
}
