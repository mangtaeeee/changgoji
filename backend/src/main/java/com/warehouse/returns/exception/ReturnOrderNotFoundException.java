package com.warehouse.returns.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class ReturnOrderNotFoundException extends BusinessException {

    public ReturnOrderNotFoundException() {
        super(ErrorCode.RETURN_ORDER_NOT_FOUND);
    }
}
