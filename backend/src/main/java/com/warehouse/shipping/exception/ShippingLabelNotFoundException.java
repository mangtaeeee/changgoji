package com.warehouse.shipping.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class ShippingLabelNotFoundException extends BusinessException {

    public ShippingLabelNotFoundException() {
        super(ErrorCode.SHIPPING_LABEL_NOT_FOUND);
    }
}
