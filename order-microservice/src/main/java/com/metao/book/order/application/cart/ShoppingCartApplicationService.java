package com.metao.book.order.application.cart;

import java.math.BigDecimal;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartApplicationService implements ShoppingCartUseCase {

    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartView getCartForUser(String userId) {
        return shoppingCartService.getCartForUser(userId);
    }

    @Override
    public int addItemToCart(String userId, Set<ShoppingCartItem> items) {
        return shoppingCartService.addItemToCart(userId, items);
    }

    @Override
    public ShoppingCartView updateItemQuantity(String userId, String sku, BigDecimal quantity) {
        ShoppingCartItem item = shoppingCartService.updateItemQuantity(userId, sku, quantity);
        if (item == null) {
            return null;
        }
        return new ShoppingCartView(
            userId,
            Set.of(new ShoppingCartItem(
                item.sku(), item.productTitle(), item.quantity(), item.price(), item.currency()
            ))
        );
    }

    @Override
    public void removeItemFromCart(String userId, String sku) {
        shoppingCartService.removeItemFromCart(userId, sku);
    }

    @Override
    public void clearCart(String userId) {
        shoppingCartService.clearCart(userId);
    }
}
