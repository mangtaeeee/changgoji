package com.warehouse.shipping.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "송장 생성 요청")
public record ShippingLabelCreateRequest(
    @Schema(description = "출고 지시 ID", example = "1")
    @NotNull Long outboundOrderId,
    @Schema(description = "택배사", example = "CJ대한통운")
    @NotBlank String carrier,
    @Schema(description = "수령인 이름", example = "홍길동")
    @NotBlank String receiverName,
    @Schema(description = "수령인 연락처", example = "010-1234-5678")
    @NotBlank String receiverPhone,
    @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123")
    @NotBlank String receiverAddress
) {
}
