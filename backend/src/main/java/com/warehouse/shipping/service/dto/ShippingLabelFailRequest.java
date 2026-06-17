package com.warehouse.shipping.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "송장 출력 실패 처리 요청")
public record ShippingLabelFailRequest(
    @Schema(description = "출력 실패 사유", example = "프린터 연결 실패")
    String failureReason
) {
}
