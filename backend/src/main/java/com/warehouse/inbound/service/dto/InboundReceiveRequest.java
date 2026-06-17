package com.warehouse.inbound.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "실사 입고 처리 요청")
public record InboundReceiveRequest(
    @Schema(description = "실사 처리할 입고 품목 목록")
    @Valid @NotEmpty List<InboundReceivedItemRequest> items
) {
}
