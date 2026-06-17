package com.warehouse.common.exception;

public class InvalidStatusException extends BusinessException {

    public InvalidStatusException() {
        super(ErrorCode.INVALID_STATUS);
    }
}
