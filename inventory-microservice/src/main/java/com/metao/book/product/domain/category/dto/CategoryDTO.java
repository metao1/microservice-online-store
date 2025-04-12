package com.metao.book.product.domain.category.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;

public record CategoryDTO(@NotNull String category) {

    @JsonCreator
    public static CategoryDTO fromString(String category) {
        return new CategoryDTO(category);
    }
}
