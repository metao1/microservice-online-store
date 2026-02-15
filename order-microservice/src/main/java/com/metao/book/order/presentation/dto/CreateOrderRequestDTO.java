package com.metao.book.order.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequestDTO(@NotBlank(message = "User ID cannot be blank")
                                    @JsonProperty("user_id")
                                    String userId) {
}
