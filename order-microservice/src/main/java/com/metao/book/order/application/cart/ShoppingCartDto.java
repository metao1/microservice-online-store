package com.metao.book.order.application.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record ShoppingCartDto(
    @JsonProperty("user_id")
    String userId,
    @JsonProperty("shopping_cart_items")
    Set<ShoppingCartItem> shoppingCartItems) {
}
