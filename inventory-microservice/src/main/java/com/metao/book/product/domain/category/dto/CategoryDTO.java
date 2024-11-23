package com.metao.book.product.domain.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryDTO(@NotNull String category) {
}
