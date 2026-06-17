package com.warehouse.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "INVENTORY_001", "재고가 부족합니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "ORDER_001", "유효하지 않은 상태 전이입니다."),
    CONCURRENT_UPDATE(HttpStatus.CONFLICT, "COMMON_001", "동시 요청으로 인해 처리에 실패했습니다. 다시 시도해주세요."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_002", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다."),
    INBOUND_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "INBOUND_001", "입고 지시를 찾을 수 없습니다."),
    OUTBOUND_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OUTBOUND_001", "출고 지시를 찾을 수 없습니다."),
    RETURN_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "RETURN_001", "반품 지시를 찾을 수 없습니다."),
    PUTAWAY_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "PUTAWAY_001", "적치 작업을 찾을 수 없습니다."),
    PICKING_WAVE_NOT_FOUND(HttpStatus.NOT_FOUND, "PICKING_001", "피킹 웨이브를 찾을 수 없습니다."),
    PICKING_TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "PICKING_002", "피킹 작업을 찾을 수 없습니다."),
    SHIPPING_LABEL_NOT_FOUND(HttpStatus.NOT_FOUND, "SHIPPING_001", "송장을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
