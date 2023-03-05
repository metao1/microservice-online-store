package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import java.util.Set;

public interface ShoppingCartService {

    void addOrderToShoppingCart(ShoppingCart shoppingCart);

    void updateOrdersInCart(ShoppingCart shopping);

    Set<ShoppingCart> getProductsInCartByUserId(String userId);

    void removeProductFromCart(String userId, String asin);

    int clearCart(String userId);

    boolean submitProducts(String userId);
}
