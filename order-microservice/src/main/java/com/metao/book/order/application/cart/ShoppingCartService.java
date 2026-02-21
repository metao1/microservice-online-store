package com.metao.book.order.application.cart;

import com.metao.book.order.domain.exception.ShoppingCartNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    public ShoppingCartDto getCartForUser(String userId) {
        var items = shoppingCartRepository.findByUserId(userId);
        var cartItems = items.stream()
            .map(item -> new ShoppingCartItem(
                item.getSku(),
                item.getQuantity(),
                item.getSellPrice(), // Assuming sellPrice is the price to display
                item.getCurrency()
            )).toList();
        return new ShoppingCartDto(
            userId,
            Set.copyOf(cartItems)
        );
    }

    @Transactional
    public int addItemToCart(
        String userId,
        @Valid @NotNull Set<ShoppingCartItem> shoppingCartItems
    ) {
        if (shoppingCartItems.isEmpty()) {
            return 0;
        }

        long now = OffsetDateTime.now().toInstant().toEpochMilli();
        Set<String> skus = shoppingCartItems.stream()
            .map(ShoppingCartItem::sku)
            .collect(Collectors.toSet());
        Map<String, ShoppingCart> existingBySku = shoppingCartRepository.findByUserIdAndSkuIn(userId, skus).stream()
            .collect(Collectors.toMap(ShoppingCart::getSku, Function.identity()));

        var items = shoppingCartItems.stream()
            .map(item -> {
                ShoppingCart existingItem = existingBySku.get(item.sku());
                if (existingItem != null) {
                    existingItem.setQuantity(existingItem.getQuantity().add(item.quantity()));
                    existingItem.setUpdatedOn(now);
                    return existingItem;
                }

                ShoppingCart newItem = new ShoppingCart(
                    userId,
                    item.sku(),
                    item.price(),
                    item.price(),
                    item.quantity(),
                    item.currency()
                );
                newItem.setUpdatedOn(now);
                return newItem;
            })
            .toList();

        shoppingCartRepository.saveAll(items);
        return items.size();
    }

    @Transactional
    public ShoppingCart updateItemQuantity(String userId, String sku, BigDecimal newQuantity) {
        ShoppingCart item = shoppingCartRepository.findByUserIdAndSku(userId, sku)
            .orElseThrow(() -> new ShoppingCartNotFoundException(String.format("user %s and sku %s", userId, sku)));

        if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            shoppingCartRepository.deleteByUserIdAndSku(userId, sku);
            return null;
        } else {
            item.setQuantity(newQuantity);
            item.setUpdatedOn(OffsetDateTime.now().toInstant().toEpochMilli()); // Corrected timestamp
            return shoppingCartRepository.save(item);
        }
    }

    @Transactional
    public void removeItemFromCart(String userId, String sku) {
        // Ensure item exists before attempting to delete to provide a clear exception if not.
        shoppingCartRepository.findByUserIdAndSku(userId, sku)
            .orElseThrow(() -> new ShoppingCartNotFoundException(String.format("user %s and sku %s", userId, sku)));
        shoppingCartRepository.deleteByUserIdAndSku(userId, sku);
    }

    @Transactional
    public void clearCart(String userId) {
        shoppingCartRepository.deleteByUserId(userId);
    }
}
