package com.metao.book.order.application.cart;

import com.metao.book.shared.architecture.ApplicationUseCase;
import java.math.BigDecimal;
import java.util.Set;

@ApplicationUseCase
public interface ShoppingCartUseCase {

    ShoppingCartView getCartForUser(String userId);

    int addItemToCart(String userId, Set<ShoppingCartItem> items);

    ShoppingCartView updateItemQuantity(String userId, String sku, BigDecimal quantity);

    void removeItemFromCart(String userId, String sku);

    void clearCart(String userId);
}
