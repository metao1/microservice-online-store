package com.metao.book.checkout.service;

import com.metao.book.checkout.domain.ShoppingCart;
import java.util.Set;

public interface ShoppingCartService {

    void addOrderToShoppingCart(ShoppingCart shoppingCart);

    Set<ShoppingCart> getProductsInCartByUserId(String userId);

    void removeProductFromCart(String userId, String asin);

    int clearCart(String userId);

    boolean submitProducts(String userId);
}
