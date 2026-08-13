package com.metao.book.order.application.port;

import com.metao.book.order.application.cart.ShoppingCartView;
import com.metao.book.order.application.cart.ShoppingCartItem;
import java.util.List;

public interface ShoppingCartCommandPort {

    void clearCart(String userId);

    int addItemToCart(String userId, List<ShoppingCartItem> items);

    ShoppingCartView getCartForUser(String userId);
}
