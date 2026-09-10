package com.metao.book.order.presentation.dto;

import com.metao.book.order.domain.model.valueobject.OrderStatus;

public record UpdateStatusRequestDto(String status) {

    public OrderStatus statusEnum() {
        return OrderStatus.valueOf(status.toUpperCase());
    }
}
