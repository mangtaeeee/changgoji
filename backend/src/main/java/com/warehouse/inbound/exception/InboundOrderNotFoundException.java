package com.warehouse.inbound.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class InboundOrderNotFoundException extends BusinessException {

    public InboundOrderNotFoundException() {
        super(ErrorCode.INBOUND_ORDER_NOT_FOUND);
    }
}
