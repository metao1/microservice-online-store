package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import java.util.List;
import java.util.Map;

public interface ShoppingCartService {

    void addProductToShoppingCart(String userId, String asin);

    Map<String, List<ShoppingCart>> getProductsInCartByUserId(String userId);

    void removeProductFromCart(String userId, String asin);

    int clearCart(String userId);

    String submitProducts(String userId);
}
