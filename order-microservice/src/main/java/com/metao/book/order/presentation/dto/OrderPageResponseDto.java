package com.metao.book.order.presentation.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record OrderPageResponseDto(
    List<OrderResponseDto> items,
    int offset,
    int limit,
    long total,
    boolean hasNext,
    boolean hasPrevious
) {

    public static OrderPageResponseDto from(Page<OrderResponseDto> page, int offset, int limit) {
        return new OrderPageResponseDto(
            page.getContent(),
            offset,
            limit,
            page.getTotalElements(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
