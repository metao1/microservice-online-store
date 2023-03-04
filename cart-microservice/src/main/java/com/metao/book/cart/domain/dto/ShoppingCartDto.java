package com.metao.book.cart.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record ShoppingCartDto
    (
        @JsonProperty("created_on")
        Long createdOn,
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("shopping_cart_items")
        Set<ShoppingCartItem> shoppingCartItems) {

    public void addItem(ShoppingCartItem shoppingCartItem) {
        this.shoppingCartItems.add(shoppingCartItem);
    }
}
