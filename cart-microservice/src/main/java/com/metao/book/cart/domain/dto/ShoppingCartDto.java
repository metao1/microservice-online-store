package com.metao.book.cart.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record ShoppingCartDto
    (
        Long createdOn,
        @JsonProperty("shopping_cart_items")
        Set<ShoppingCartItem> shoppingCartItems) {

    public void addItem(ShoppingCartItem shoppingCartItem) {
        this.shoppingCartItems.add(shoppingCartItem);
    }
}
