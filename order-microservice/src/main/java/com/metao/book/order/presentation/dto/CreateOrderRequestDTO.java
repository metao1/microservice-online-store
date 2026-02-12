package com.metao.book.order.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record CreateOrderRequestDTO(@NotBlank(message = "User ID cannot be blank")
                                    @JsonProperty("user_id")
                                    String userId) {
}
