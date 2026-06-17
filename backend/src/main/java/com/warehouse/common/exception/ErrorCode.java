package com.warehouse.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INSUFFICIENT_STOCK("INVENTORY_001", "재고가 부족합니다."),
    INVALID_STATUS("ORDER_001", "유효하지 않은 상태 전이입니다."),
    CONCURRENT_UPDATE("COMMON_001", "동시 요청으로 인해 처리에 실패했습니다. 다시 시도해주세요."),
    INVALID_INPUT("COMMON_002", "입력값이 올바르지 않습니다."),
    INBOUND_ORDER_NOT_FOUND("INBOUND_001", "입고 지시를 찾을 수 없습니다."),
    OUTBOUND_ORDER_NOT_FOUND("OUTBOUND_001", "출고 지시를 찾을 수 없습니다."),
    RETURN_ORDER_NOT_FOUND("RETURN_001", "반품 지시를 찾을 수 없습니다."),
    PUTAWAY_TASK_NOT_FOUND("PUTAWAY_001", "적치 작업을 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
