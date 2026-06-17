package com.warehouse.shipping.service.dto;

import com.warehouse.shipping.domain.ShippingLabel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "송장 응답")
public record ShippingLabelResponse(
    @Schema(description = "송장 ID", example = "1")
    Long id,
    @Schema(description = "출고 지시 ID", example = "1")
    Long outboundOrderId,
    @Schema(description = "운송장 번호", example = "CJ20260617000001")
    String trackingNo,
    @Schema(description = "택배사", example = "CJ대한통운")
    String carrier,
    @Schema(description = "수령인 이름", example = "홍길동")
    String receiverName,
    @Schema(description = "수령인 연락처", example = "010-1234-5678")
    String receiverPhone,
    @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123")
    String receiverAddress,
    @Schema(description = "송장 출력 상태", example = "PENDING")
    String status,
    @Schema(description = "출력 실패 사유", example = "프린터 연결 실패")
    String failureReason,
    @Schema(description = "송장 출력용 데이터 JSON 문자열")
    String labelData
) {

    public static ShippingLabelResponse from(ShippingLabel label) {
        return new ShippingLabelResponse(
            label.getId(),
            label.getOutboundOrderId(),
            label.getTrackingNo(),
            label.getCarrier(),
            label.getReceiverName(),
            label.getReceiverPhone(),
            label.getReceiverAddress(),
            label.getStatus().name(),
            label.getFailureReason(),
            label.getLabelData()
        );
    }
}
