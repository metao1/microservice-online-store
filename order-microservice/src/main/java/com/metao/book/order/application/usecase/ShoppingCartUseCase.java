package com.metao.book.order.application.usecase;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartView;
import com.metao.book.shared.architecture.ApplicationUseCase;
import java.math.BigDecimal;
import java.util.List;

@ApplicationUseCase
public interface ShoppingCartUseCase {

    ShoppingCartView getCartForUser(String userId);

    int addItemToCart(String userId, List<ShoppingCartItem> items);

    ShoppingCartView updateItemQuantity(String userId, String sku, BigDecimal quantity);

    void removeItemFromCart(String userId, String sku);

    void clearCart(String userId);
}
