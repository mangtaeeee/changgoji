package com.warehouse.inventory.exception;

import com.warehouse.common.exception.BusinessException;
import com.warehouse.common.exception.ErrorCode;

public class ConcurrentStockUpdateException extends BusinessException {

    public ConcurrentStockUpdateException() {
        super(ErrorCode.CONCURRENT_UPDATE, "동시 재고 변경 요청이 반복 충돌했습니다. 잠시 후 다시 시도해주세요.");
    }
}
