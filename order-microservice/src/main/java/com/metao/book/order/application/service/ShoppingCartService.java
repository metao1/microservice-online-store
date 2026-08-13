package com.metao.book.order.application.service;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.cart.ShoppingCartView;
import com.metao.book.order.application.port.ShoppingCartCommandPort;
import com.metao.book.order.application.port.ShoppingCartPort;
import com.metao.book.order.domain.exception.ShoppingCartNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService implements ShoppingCartCommandPort {

    private final ShoppingCartPort shoppingCartPort;

    public ShoppingCartView getCartForUser(String userId) {
        var items = shoppingCartPort.findByUserId(userId);
        var cartItems = items.stream()
            .toList();
        return new ShoppingCartView(
            userId,
            List.copyOf(cartItems)
        );
    }

    @Transactional
    public int addItemToCart(
        String userId,
        @NotNull List<ShoppingCartItem> shoppingCartItems
    ) {
        if (shoppingCartItems.isEmpty()) {
            return 0;
        }

        Set<String> skus = shoppingCartItems.stream()
            .map(ShoppingCartItem::sku)
            .collect(Collectors.toSet());
        Set<String> existingSkus = shoppingCartPort.findByUserIdAndSkuIn(userId, skus).stream()
            .map(ShoppingCartItem::sku)
            .collect(Collectors.toSet());

        var items = shoppingCartItems.stream()
            .<ShoppingCartItem>mapMulti((item, consumer) -> {
                if (!existingSkus.contains(item.sku())) {
                    var productItem = shoppingCartPort.findByUserIdAndSku(userId, item.sku()).orElse(item);
                    consumer.accept(productItem);
                }
            }).collect(Collectors.toSet());

        shoppingCartPort.saveAll(userId, items);
        return items.size();
    }

    @Transactional
    public ShoppingCartItem updateItemQuantity(String userId, String sku, BigDecimal newQuantity) {
        ShoppingCartItem item = shoppingCartPort.findByUserIdAndSku(userId, sku)
            .orElseThrow(() -> new ShoppingCartNotFoundException(String.format("user %s and sku %s", userId, sku)));

        if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            shoppingCartPort.deleteByUserIdAndSku(userId, sku);
            return null;
        } else {
            return shoppingCartPort.save(userId, new ShoppingCartItem(
                item.sku(), item.productTitle(), newQuantity, item.price(), item.currency()));
        }
    }

    @Transactional
    public void removeItemFromCart(String userId, String sku) {
        // Ensure item exists before attempting to delete to provide a clear exception if not.
        shoppingCartPort.findByUserIdAndSku(userId, sku)
            .orElseThrow(() -> new ShoppingCartNotFoundException(String.format("user %s and sku %s", userId, sku)));
        shoppingCartPort.deleteByUserIdAndSku(userId, sku);
    }

    @Transactional
    public void clearCart(String userId) {
        shoppingCartPort.deleteByUserId(userId);
    }
}
