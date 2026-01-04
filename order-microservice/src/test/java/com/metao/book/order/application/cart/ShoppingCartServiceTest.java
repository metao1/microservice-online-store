package com.metao.book.order.application.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.domain.exception.ShoppingCartNotFoundException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TWO = BigDecimal.TWO;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    private String userId;
    private String sku;
    private ShoppingCart cartItem;
    private Currency currency;

    @BeforeEach
    void setUp() {
        userId = "testUser";
        sku = "B00TESTSKU";
        currency = Currency.getInstance("USD");
        // Constructor: public ShoppingCart(String userId, String sku, BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal quantity, Currency currency)
        cartItem = new ShoppingCart(userId, sku, BigDecimal.valueOf(10.00), BigDecimal.valueOf(10.00), ONE, currency);
        // Manually set createdOn and updatedOn as the service might rely on them, and constructor sets createdOn.
        // The service sets updatedOn to createdOn for new items in addItemToCart.
        // For existing items, updatedOn is set when quantity changes.
        cartItem.setCreatedOn(System.currentTimeMillis());
        cartItem.setUpdatedOn(cartItem.getCreatedOn());
    }

    // --- Tests for getCartForUser ---
    @Test
    void getCartForUser_whenCartExists_returnsCartDto() {
        when(shoppingCartRepository.findByUserId(userId)).thenReturn(List.of(cartItem));

        ShoppingCartDto resultDto = shoppingCartService.getCartForUser(userId);

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.userId()).isEqualTo(userId);
        assertThat(resultDto.shoppingCartItems()).hasSize(1);
        ShoppingCartItem resultItem = resultDto.shoppingCartItems().iterator().next();
        assertThat(resultItem.sku()).isEqualTo(sku);
        assertThat(resultItem.quantity()).isEqualByComparingTo(ONE); // Use isEqualByComparingTo for BigDecimal
        assertThat(resultItem.price()).isEqualByComparingTo(
            BigDecimal.valueOf(10.00)); // Assuming sellPrice is mapped to price
    }

    @Test
    void getCartForUser_whenCartIsEmpty_returnsDtoWithEmptyItems() {
        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        ShoppingCartDto resultDto = shoppingCartService.getCartForUser(userId);

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.userId()).isEqualTo(userId);
        assertThat(resultDto.shoppingCartItems()).isEmpty();
    }

    // --- Tests for addItemToCart ---
    @Test
    void addItemToCart_whenItemIsNew_createsAndSavesItem() {
        // The service will create a new ShoppingCart object. We mock saveAll to verify the saved items.
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.empty());
        // Return the objects that would be saved by the service
        when(shoppingCartRepository.saveAll(any(List.class))).thenAnswer(invocation -> {
            List<ShoppingCart> savedItems = invocation.getArgument(0);
            // Verify the saved items
            assertThat(savedItems).hasSize(1);
            ShoppingCart savedItem = savedItems.get(0);
            assertThat(savedItem.getUserId()).isEqualTo(userId);
            assertThat(savedItem.getSku()).isEqualTo(sku);
            assertThat(savedItem.getQuantity()).isEqualByComparingTo(ONE);
            assertThat(savedItem.getBuyPrice()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(savedItem.getSellPrice()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(savedItem.getCurrency()).isEqualTo(currency);
            assertThat(savedItem.getCreatedOn()).isNotNull();
            assertThat(savedItem.getUpdatedOn()).isEqualTo(savedItem.getCreatedOn()); // Service logic for a new item
            return savedItems; // Return the actual saved items
        });

        var result = shoppingCartService.addItemToCart(userId,
            Set.of(
                new ShoppingCartItem(sku, ONE, BigDecimal.valueOf(10.00), currency)
            ));

        assertThat(result).isPositive();
        assertThat(result).isEqualByComparingTo(1);
    }

    @Test
    void addItemToCart_whenItemExists_updatesQuantityAndSaves() {
        ShoppingCart existingItem = new ShoppingCart(userId, sku, BigDecimal.valueOf(10.00), BigDecimal.valueOf(10.00),
            ONE, currency);
        long originalUpdatedOn = System.currentTimeMillis() - 1000; // ensure updatedOn changes
        existingItem.setUpdatedOn(originalUpdatedOn);

        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.of(existingItem));
        when(shoppingCartRepository.saveAll(any(List.class))).thenAnswer(invocation -> {
            List<ShoppingCart> savedItems = invocation.getArgument(0);
            // Verify the saved item
            assertThat(savedItems).hasSize(1); // Set deduplicates identical items
            ShoppingCart savedItem = savedItems.get(0);
            assertThat(savedItem.getUserId()).isEqualTo(userId);
            assertThat(savedItem.getSku()).isEqualTo(sku);
            assertThat(savedItem.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3)); // 1 + 2
            assertThat(savedItem.getUpdatedOn()).isGreaterThan(originalUpdatedOn);
            return savedItems;
        });

        var result = shoppingCartService.addItemToCart(userId,
            Set.of(
                new ShoppingCartItem(sku, TWO, BigDecimal.valueOf(10.00), currency)
            )
        ); // Adding 2 to existing quantity of 1

        assertThat(result).isPositive();
        assertThat(result).isEqualByComparingTo(1); // 1 item processed
    }

    // --- Tests for updateItemQuantity ---
    @Test
    void updateItemQuantity_whenItemExistsAndQuantityPositive_updatesAndSaves() {
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.of(cartItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(cartItem);
        BigDecimal newQuantity = BigDecimal.valueOf(5);
        long originalUpdatedOn = cartItem.getUpdatedOn();

        ShoppingCart result = shoppingCartService.updateItemQuantity(userId, sku, newQuantity);

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualByComparingTo(newQuantity);
        assertThat(result.getUpdatedOn()).isGreaterThanOrEqualTo(originalUpdatedOn);
        verify(shoppingCartRepository).save(cartItem);
    }

    @Test
    void updateItemQuantity_whenQuantityIsZero_removesItem() {
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.of(cartItem));
        doNothing().when(shoppingCartRepository).deleteByUserIdAndSku(userId, sku);

        ShoppingCart result = shoppingCartService.updateItemQuantity(userId, sku, BigDecimal.ZERO);

        assertThat(result).isNull();
        verify(shoppingCartRepository).deleteByUserIdAndSku(userId, sku);
    }

    @Test
    void updateItemQuantity_whenQuantityIsNegative_removesItem() {
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.of(cartItem));
        doNothing().when(shoppingCartRepository).deleteByUserIdAndSku(userId, sku);

        ShoppingCart result = shoppingCartService.updateItemQuantity(userId, sku, BigDecimal.valueOf(-1));

        assertThat(result).isNull();
        verify(shoppingCartRepository).deleteByUserIdAndSku(userId, sku);
    }

    @Test
    void updateItemQuantity_whenItemNotFound_throwsException() {
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.empty());
        var newQuantity = BigDecimal.valueOf(3);

        assertThrows(ShoppingCartNotFoundException.class,
            () -> shoppingCartService.updateItemQuantity(userId, sku, newQuantity));
    }

    // --- Tests for removeItemFromCart ---
    @Test
    void removeItemFromCart_whenItemExists_removesItem() {
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(Optional.of(cartItem)); // Item exists
        doNothing().when(shoppingCartRepository).deleteByUserIdAndSku(userId, sku);

        shoppingCartService.removeItemFromCart(userId, sku);

        verify(shoppingCartRepository).deleteByUserIdAndSku(userId, sku);
    }

    @Test
    void removeItemFromCart_whenItemNotFound_throwsException() {
        when(shoppingCartRepository.findByUserIdAndSku(userId, sku)).thenReturn(
            Optional.empty()); // Item does not exist

        assertThrows(ShoppingCartNotFoundException.class, () -> shoppingCartService.removeItemFromCart(userId, sku));
        verify(shoppingCartRepository, never()).deleteByUserIdAndSku(anyString(), anyString());
    }

    // --- Tests for clearCart ---
    @Test
    void clearCart_removesAllItemsForUser() {
        doNothing().when(shoppingCartRepository).deleteByUserId(userId);

        shoppingCartService.clearCart(userId);

        verify(shoppingCartRepository).deleteByUserId(userId);
    }
}

