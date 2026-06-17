package com.warehouse.outbound.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class OutboundOrderNotFoundException extends BusinessException {

    public OutboundOrderNotFoundException() {
        super(ErrorCode.OUTBOUND_ORDER_NOT_FOUND);
    }
}
