package com.metao.book.order.application.cart;

import com.metao.book.order.domain.exception.OrderNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.HashSet;
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
        // The grand total calculation will be handled by the client or a future enhancement.
        // The ShoppingCartDto is a record: ShoppingCartDto(Long createdOn, String userId, Set<ShoppingCartItem> shoppingCartItems)
        return new ShoppingCartDto(userId, new HashSet<>(cartItems));
    }

    @Transactional
    public ShoppingCart addItemToCart(String userId, String sku, int quantity, BigDecimal price, Currency currency) {
        ShoppingCart item = shoppingCartRepository.findByUserIdAndSku(userId, sku)
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    existingItem.setUpdatedOn(OffsetDateTime.now().toInstant().toEpochMilli()); // Corrected timestamp
                    return existingItem;
                })
                .orElseGet(() -> {
                    ShoppingCart newItem = new ShoppingCart(userId, sku, price, price, quantity, currency);
                    newItem.setUpdatedOn(newItem.getCreatedOn()); // Set updatedOn to createdOn for new items
                    return newItem;
                });
        return shoppingCartRepository.save(item);
    }

    @Transactional
    public ShoppingCart updateItemQuantity(String userId, String sku, int newQuantity) {
        ShoppingCart item = shoppingCartRepository.findByUserIdAndSku(userId, sku)
            .orElseThrow(() -> new OrderNotFoundException(
                String.format("Cart item not found for user %s and sku %s", userId, sku)));

        if (newQuantity <= 0) {
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
        // Ensure item exists before attempting delete to provide a clear exception if not.
        shoppingCartRepository.findByUserIdAndSku(userId, sku)
                .orElseThrow(() -> new OrderNotFoundException("Cart item not found for user " + userId + " and sku " + sku + " when attempting to remove."));
        shoppingCartRepository.deleteByUserIdAndSku(userId, sku);
    }

    @Transactional
    public void clearCart(String userId) {
        shoppingCartRepository.deleteByUserId(userId);
    }
}
