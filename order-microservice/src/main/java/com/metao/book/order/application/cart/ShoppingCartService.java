package com.metao.book.order.application.cart;

import com.metao.book.order.domain.exception.OrderNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    public ShoppingCartDto getCartForUser(String userId) {
        List<ShoppingCart> items = shoppingCartRepository.findByUserId(userId);
        List<ShoppingCartItem> cartItems = items.stream()
                .map(item -> new ShoppingCartItem(
                        item.getAsin(),
                        item.getQuantity(),
                        item.getSellPrice(), // Assuming sellPrice is the price to display
                        item.getCurrency()
                )).toList();
        // The grand total calculation will be handled by the client or a future enhancement.
        // The ShoppingCartDto is a record: ShoppingCartDto(Long createdOn, String userId, Set<ShoppingCartItem> shoppingCartItems)
        return new ShoppingCartDto(null, userId, new HashSet<>(cartItems));
    }

    @Transactional
    public ShoppingCart addItemToCart(String userId, String asin, BigDecimal quantity, BigDecimal price, Currency currency) {
        ShoppingCart item = shoppingCartRepository.findByUserIdAndAsin(userId, asin)
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity().add(quantity));
                    existingItem.setUpdatedOn(OffsetDateTime.now().toInstant().toEpochMilli()); // Corrected timestamp
                    return existingItem;
                })
                .orElseGet(() -> {
                    ShoppingCart newItem = new ShoppingCart(userId, asin, price, price, quantity, currency);
                    newItem.setUpdatedOn(newItem.getCreatedOn()); // Set updatedOn to createdOn for new items
                    return newItem;
                });
        return shoppingCartRepository.save(item);
    }

    @Transactional
    public ShoppingCart updateItemQuantity(String userId, String asin, BigDecimal newQuantity) {
        ShoppingCart item = shoppingCartRepository.findByUserIdAndAsin(userId, asin)
            .orElseThrow(() -> new OrderNotFoundException(
                String.format("Cart item not found for user %s and asin %s", userId, asin)));

        if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            shoppingCartRepository.deleteByUserIdAndAsin(userId, asin);
            return null;
        } else {
            item.setQuantity(newQuantity);
            item.setUpdatedOn(OffsetDateTime.now().toInstant().toEpochMilli()); // Corrected timestamp
            return shoppingCartRepository.save(item);
        }
    }

    @Transactional
    public void removeItemFromCart(String userId, String asin) {
        // Ensure item exists before attempting delete to provide a clear exception if not.
        shoppingCartRepository.findByUserIdAndAsin(userId, asin)
                .orElseThrow(() -> new OrderNotFoundException("Cart item not found for user " + userId + " and asin " + asin + " when attempting to remove."));
        shoppingCartRepository.deleteByUserIdAndAsin(userId, asin);
    }

    @Transactional
    public void clearCart(String userId) {
        shoppingCartRepository.deleteByUserId(userId);
    }
}
