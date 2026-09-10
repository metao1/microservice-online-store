package com.metao.book.order.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.application.cart.ShoppingCartView;
import java.util.List;

public record ShoppingCartResponseDto(
    @JsonProperty("user_id") String userId,
    @JsonProperty("shopping_cart_items") List<ShoppingCartItemDto> shoppingCartItems
) {

    public static ShoppingCartResponseDto from(ShoppingCartView view) {
        return new ShoppingCartResponseDto(
            view.userId(), view.shoppingCartItems().stream().map(ShoppingCartItemDto::from).toList());
    }
}
