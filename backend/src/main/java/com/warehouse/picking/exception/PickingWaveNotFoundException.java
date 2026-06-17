package com.warehouse.picking.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class PickingWaveNotFoundException extends BusinessException {

    public PickingWaveNotFoundException() {
        super(ErrorCode.PICKING_WAVE_NOT_FOUND);
    }
}
