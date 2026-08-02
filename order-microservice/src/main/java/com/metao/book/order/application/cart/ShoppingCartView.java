package com.metao.book.order.application.cart;

import java.util.Set;

public record ShoppingCartView(String userId, Set<ShoppingCartItem> shoppingCartItems) {
}
