package com.metao.product.cart.service;

import com.metao.product.cart.domain.ShoppingCart;
import com.metao.product.cart.domain.ShoppingCartKey;

import java.time.LocalDateTime;
import java.util.Map;

public interface ShoppingCartService {

    int DEFAULT_QUANTITY = 1;

    public void addProductToShoppingCart(String userId, String asin);

    public Map<String, Integer> getProductsInCart(String userId);

    public void removeProductFromCart(String userId, String asin);

    default ShoppingCart createCartObject(ShoppingCartKey currentKey) {
        ShoppingCart currentShoppingCart = new ShoppingCart();
        currentShoppingCart.setCartKey(currentKey.getId() + "-" + currentKey.getAsin());
        currentShoppingCart.setUserId(currentKey.getId());
        currentShoppingCart.setAsin(currentKey.getAsin());
        LocalDateTime currentTime = LocalDateTime.now();
        currentShoppingCart.setTime_added(currentTime.toString());
        currentShoppingCart.setQuantity(DEFAULT_QUANTITY);

        return currentShoppingCart;
    }

    public void clearCart(String userId);
}
