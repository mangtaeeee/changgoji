package com.warehouse.shipping.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.warehouse.common.exception.InvalidStatusException;
import org.junit.jupiter.api.Test;

class ShippingLabelTest {

    @Test
    void markPrinted_throwsWhenPrintWasNotRequested() {
        ShippingLabel label = ShippingLabel.create(
            1L,
            "CJ20260619000001",
            "CJ대한통운",
            "홍길동",
            "010-1234-5678",
            "서울시 강남구 테헤란로 123",
            "{\"trackingNo\":\"CJ20260619000001\"}"
        );

        assertThatThrownBy(label::markPrinted)
            .isInstanceOf(InvalidStatusException.class);
    }
}
