package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDateTime.now;

public interface ShoppingCartService {

    int DEFAULT_QUANTITY = 1;

    void addProductToShoppingCart(String userId, String asin);

    Map<String, Integer> getProductsInCart(String userId);

    void removeProductFromCart(String userId, String asin);

    default ShoppingCart createCartObject(ShoppingCartKey currentKey) {
        ShoppingCart currentShoppingCart = new ShoppingCart();
        currentShoppingCart.setCartKey(currentKey.getId() + "-" + currentKey.getAsin());
        currentShoppingCart.setUserId(currentKey.getId());
        currentShoppingCart.setAsin(currentKey.getAsin());
        LocalDateTime currentTime = now();
        currentShoppingCart.setTime_added(currentTime.toString());
        currentShoppingCart.setQuantity(DEFAULT_QUANTITY);

        return currentShoppingCart;
    }

    public int clearCart(String userId);
}
