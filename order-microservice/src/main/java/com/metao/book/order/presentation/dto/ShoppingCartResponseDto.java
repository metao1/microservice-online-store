package com.metao.book.order.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.order.application.cart.ShoppingCartView;
import java.util.Set;

public record ShoppingCartResponseDto(
    @JsonProperty("user_id") String userId,
    @JsonProperty("shopping_cart_items") Set<com.metao.book.order.application.cart.ShoppingCartItem> shoppingCartItems
) {

    public static ShoppingCartResponseDto from(ShoppingCartView view) {
        return new ShoppingCartResponseDto(view.userId(), view.shoppingCartItems());
    }
}
