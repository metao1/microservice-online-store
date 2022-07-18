package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface ShoppingCartService {

    int DEFAULT_QUANTITY = 1;

    void addProductToShoppingCart(String userId, String asin);

    Map<String, List<ShoppingCart>> getProductsInCartByUserId(String userId);

    void removeProductFromCart(String userId, String asin);

    default ShoppingCart createCart(ShoppingCartKey currentKey) {
        return ShoppingCart
                .builder()
                .userId(currentKey.getUserId() + "-" + currentKey.getAsin())
                .asin(currentKey.getAsin())
                .quantity(DEFAULT_QUANTITY)
                .timeAdded(Instant.now())
                .build();
    }

    int clearCart(String userId);
}
