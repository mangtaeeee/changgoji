package com.warehouse.inbound.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InboundReceiveRequest(
    @Valid @NotEmpty List<InboundReceivedItemRequest> items
) {
}
