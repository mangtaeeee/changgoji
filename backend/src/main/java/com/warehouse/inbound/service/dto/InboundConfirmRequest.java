package com.warehouse.inbound.service.dto;

import jakarta.validation.constraints.NotNull;

public record InboundConfirmRequest(
    @NotNull Long confirmedBy,
    String memo
) {
}
