package com.metao.book.order.application.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.domain.exception.OrderNotFoundException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    private String userId;
    private String asin;
    private ShoppingCart cartItem;
    private Currency currency;

    @BeforeEach
    void setUp() {
        userId = "testUser";
        asin = "B00TESTASIN";
        currency = Currency.getInstance("USD");
        // Constructor: public ShoppingCart(String userId, String asin, BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal quantity, Currency currency)
        cartItem = new ShoppingCart(userId, asin, BigDecimal.valueOf(10.00), BigDecimal.valueOf(10.00), BigDecimal.ONE, currency);
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
        assertThat(resultItem.asin()).isEqualTo(asin);
        assertThat(resultItem.quantity()).isEqualByComparingTo(BigDecimal.ONE); // Use isEqualByComparingTo for BigDecimal
        assertThat(resultItem.price()).isEqualByComparingTo(BigDecimal.valueOf(10.00)); // Assuming sellPrice is mapped to price
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
        // The service will create a new ShoppingCart object. We mock the save to return our reference cartItem or a similar one.
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.empty());
        // Return the object that would be created by the service, or a similar one for assertions
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> {
            ShoppingCart savedItem = invocation.getArgument(0);
            // Ensure the saved item has the correct properties set by the service
            assertThat(savedItem.getUserId()).isEqualTo(userId);
            assertThat(savedItem.getAsin()).isEqualTo(asin);
            assertThat(savedItem.getQuantity()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(savedItem.getBuyPrice()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(savedItem.getSellPrice()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(savedItem.getCurrency()).isEqualTo(currency);
            assertThat(savedItem.getCreatedOn()).isNotNull();
            assertThat(savedItem.getUpdatedOn()).isEqualTo(savedItem.getCreatedOn()); // Service logic for new item
            return savedItem; // Return the actual saved item
        });

        ShoppingCart result = shoppingCartService.addItemToCart(userId, asin, BigDecimal.ONE, BigDecimal.valueOf(10.00), currency);

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.ONE);
        verify(shoppingCartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void addItemToCart_whenItemExists_updatesQuantityAndSaves() {
        ShoppingCart existingItem = new ShoppingCart(userId, asin, BigDecimal.valueOf(10.00), BigDecimal.valueOf(10.00), BigDecimal.ONE, currency);
        long originalUpdatedOn = System.currentTimeMillis() - 1000; // ensure updatedOn changes
        existingItem.setUpdatedOn(originalUpdatedOn);

        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.of(existingItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(existingItem); // save returns the modified existingItem

        ShoppingCart result = shoppingCartService.addItemToCart(userId, asin, BigDecimal.valueOf(2), BigDecimal.valueOf(10.00), currency); // Adding 2 more

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3)); // 1 (existing) + 2 (new)
        assertThat(result.getUpdatedOn()).isGreaterThan(originalUpdatedOn); // Check that updatedOn was changed
        verify(shoppingCartRepository).save(existingItem);
    }
    
    // --- Tests for updateItemQuantity ---
    @Test
    void updateItemQuantity_whenItemExistsAndQuantityPositive_updatesAndSaves() {
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.of(cartItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(cartItem);
        BigDecimal newQuantity = BigDecimal.valueOf(5);
        long originalUpdatedOn = cartItem.getUpdatedOn();

        ShoppingCart result = shoppingCartService.updateItemQuantity(userId, asin, newQuantity);

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualByComparingTo(newQuantity);
        assertThat(result.getUpdatedOn()).isGreaterThanOrEqualTo(originalUpdatedOn);
        verify(shoppingCartRepository).save(cartItem);
    }

    @Test
    void updateItemQuantity_whenQuantityIsZero_removesItem() {
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.of(cartItem));
        doNothing().when(shoppingCartRepository).deleteByUserIdAndAsin(userId, asin);

        ShoppingCart result = shoppingCartService.updateItemQuantity(userId, asin, BigDecimal.ZERO);
        
        assertThat(result).isNull(); 
        verify(shoppingCartRepository).deleteByUserIdAndAsin(userId, asin);
    }
    
    @Test
    void updateItemQuantity_whenQuantityIsNegative_removesItem() {
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.of(cartItem));
        doNothing().when(shoppingCartRepository).deleteByUserIdAndAsin(userId, asin);

        ShoppingCart result = shoppingCartService.updateItemQuantity(userId, asin, BigDecimal.valueOf(-1));

        assertThat(result).isNull();
        verify(shoppingCartRepository).deleteByUserIdAndAsin(userId, asin);
    }

    @Test
    void updateItemQuantity_whenItemNotFound_throwsException() {
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.empty());
        BigDecimal newQuantity = BigDecimal.valueOf(5);

        assertThrows(OrderNotFoundException.class,
            () -> shoppingCartService.updateItemQuantity(userId, asin, newQuantity));
    }

    // --- Tests for removeItemFromCart ---
    @Test
    void removeItemFromCart_whenItemExists_removesItem() {
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.of(cartItem)); // Item exists
        doNothing().when(shoppingCartRepository).deleteByUserIdAndAsin(userId, asin);
        
        shoppingCartService.removeItemFromCart(userId, asin);

        verify(shoppingCartRepository).deleteByUserIdAndAsin(userId, asin);
    }

    @Test
    void removeItemFromCart_whenItemNotFound_throwsException() {
        when(shoppingCartRepository.findByUserIdAndAsin(userId, asin)).thenReturn(Optional.empty()); // Item does not exist

        assertThrows(OrderNotFoundException.class, () -> shoppingCartService.removeItemFromCart(userId, asin));
        verify(shoppingCartRepository, never()).deleteByUserIdAndAsin(anyString(), anyString());
    }
    
    // --- Tests for clearCart ---
    @Test
    void clearCart_removesAllItemsForUser() {
        doNothing().when(shoppingCartRepository).deleteByUserId(userId);

        shoppingCartService.clearCart(userId);

        verify(shoppingCartRepository).deleteByUserId(userId);
    }
}

