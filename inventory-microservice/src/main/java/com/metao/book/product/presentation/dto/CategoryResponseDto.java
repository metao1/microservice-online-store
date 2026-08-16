package com.metao.book.product.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryResponseDto(@NotNull String category) {
}
