package com.metao.book.order.application.cart;

import com.metao.book.order.domain.exception.ShoppingCartNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
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
        // loop through each item and add to cart
        var items = shoppingCartItems.stream()
            .map(item -> shoppingCartRepository.findByUserIdAndSku(userId, item.sku())
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity().add(item.quantity()));
                    existingItem.setUpdatedOn(OffsetDateTime.now().toInstant().toEpochMilli());
                    return existingItem;
                })
                .orElseGet(() -> {
                    ShoppingCart newItem = new ShoppingCart(
                        userId,
                        item.sku(),
                        item.price(),
                        item.price(),
                        item.quantity(),
                        item.currency()
                    );
                    newItem.setUpdatedOn(newItem.getCreatedOn());
                    return newItem;
                })
            ).toList();
        // Save all items to the repository
        shoppingCartRepository.saveAll(items);
        // Return the last added item as a representative (could be modified as needed)
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
