package com.warehouse.picking.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class PickingTaskNotFoundException extends BusinessException {

    public PickingTaskNotFoundException() {
        super(ErrorCode.PICKING_TASK_NOT_FOUND);
    }
}
