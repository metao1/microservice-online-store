package com.metao.book.order.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequestDTO(@NotBlank(message = "User ID cannot be blank") String userId) {
}
