package com.metao.book.order.application.cart;

import com.metao.book.order.domain.exception.ShoppingCartNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartPort shoppingCartPort;

    public ShoppingCartView getCartForUser(String userId) {
        var items = shoppingCartPort.findByUserId(userId);
        var cartItems = items.stream()
            .toList();
        return new ShoppingCartView(
            userId,
            Set.copyOf(cartItems)
        );
    }

    @Transactional
    public int addItemToCart(
        String userId,
        @NotNull Set<ShoppingCartItem> shoppingCartItems
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
            .map(item -> {
                if (existingSkus.contains(item.sku())) {
                    return shoppingCartPort.findByUserIdAndSku(userId, item.sku())
                        .map(existing -> new ShoppingCartItem(
                            existing.sku(), existing.productTitle(), existing.quantity().add(item.quantity()), existing.price(), existing.currency()))
                        .orElse(item);
                }
                return item;
            })
            .toList();

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
