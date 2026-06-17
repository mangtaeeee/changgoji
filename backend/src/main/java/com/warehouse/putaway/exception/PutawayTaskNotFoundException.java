package com.warehouse.putaway.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class PutawayTaskNotFoundException extends BusinessException {

    public PutawayTaskNotFoundException() {
        super(ErrorCode.PUTAWAY_TASK_NOT_FOUND);
    }
}
