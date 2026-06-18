package com.warehouse.inventory.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "재고 목록 페이지 응답")
public record InventoryPageResponse(
    @Schema(description = "재고 목록")
    List<InventoryListResponse> items,
    @Schema(description = "현재 페이지 번호", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "50")
    int size,
    @Schema(description = "전체 재고 수", example = "20000")
    long totalElements,
    @Schema(description = "전체 페이지 수", example = "400")
    int totalPages
) {

    public static InventoryPageResponse from(Page<InventoryListResponse> page) {
        return new InventoryPageResponse(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
