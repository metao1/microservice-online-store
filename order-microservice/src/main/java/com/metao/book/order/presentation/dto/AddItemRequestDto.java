package com.metao.book.order.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.application.cart.ShoppingCartItem;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AddItemRequestDto(
    @NotBlank @JsonProperty("user_id") String userId,
    List<ShoppingCartItem> items
) {

}