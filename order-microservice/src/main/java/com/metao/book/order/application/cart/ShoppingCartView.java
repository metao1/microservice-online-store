package com.metao.book.order.application.cart;

import java.util.List;

public record ShoppingCartView(String userId, List<ShoppingCartItem> shoppingCartItems) {
}
