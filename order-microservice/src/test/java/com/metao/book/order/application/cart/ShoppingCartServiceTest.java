package com.metao.book.order.application.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metao.book.order.domain.exception.ShoppingCartNotFoundException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    private static final String USER_ID = "testUser";
    private static final String SKU = "B00TESTSKU";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final ShoppingCartItem ITEM = new ShoppingCartItem(
        SKU, "product-123", BigDecimal.ONE, BigDecimal.TEN, CURRENCY);

    @Mock
    private ShoppingCartPort shoppingCartPort;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Test
    void getCartForUser_returnsMappedItems() {
        when(shoppingCartPort.findByUserId(USER_ID)).thenReturn(List.of(ITEM));

        ShoppingCartView result = shoppingCartService.getCartForUser(USER_ID);

        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.shoppingCartItems()).containsExactly(ITEM);
    }

    @Test
    void addItemToCart_savesNewItems() {
        when(shoppingCartPort.findByUserIdAndSkuIn(eq(USER_ID), any())).thenReturn(List.of());

        int result = shoppingCartService.addItemToCart(USER_ID, Set.of(ITEM));

        assertThat(result).isEqualTo(1);
        verify(shoppingCartPort).saveAll(USER_ID, List.of(ITEM));
    }

    @Test
    void addItemToCart_mergesExistingQuantity() {
        ShoppingCartItem existing = new ShoppingCartItem(
            SKU, ITEM.productTitle(), BigDecimal.ONE, ITEM.price(), CURRENCY);
        when(shoppingCartPort.findByUserIdAndSkuIn(eq(USER_ID), any())).thenReturn(List.of(existing));
        when(shoppingCartPort.findByUserIdAndSku(USER_ID, SKU)).thenReturn(Optional.of(existing));

        shoppingCartService.addItemToCart(USER_ID, Set.of(ITEM));

        verify(shoppingCartPort).saveAll(USER_ID, List.of(new ShoppingCartItem(
            SKU, ITEM.productTitle(), BigDecimal.TWO, ITEM.price(), CURRENCY)));
    }

    @Test
    void updateItemQuantity_savesPositiveQuantity() {
        when(shoppingCartPort.findByUserIdAndSku(USER_ID, SKU)).thenReturn(Optional.of(ITEM));
        ShoppingCartItem updated = new ShoppingCartItem(
            SKU, ITEM.productTitle(), BigDecimal.valueOf(5), ITEM.price(), CURRENCY);
        when(shoppingCartPort.save(USER_ID, updated)).thenReturn(updated);

        ShoppingCartItem result = shoppingCartService.updateItemQuantity(USER_ID, SKU, BigDecimal.valueOf(5));

        assertThat(result).isEqualTo(updated);
        verify(shoppingCartPort).save(USER_ID, updated);
    }

    @Test
    void updateItemQuantity_deletesNonPositiveQuantity() {
        when(shoppingCartPort.findByUserIdAndSku(USER_ID, SKU)).thenReturn(Optional.of(ITEM));

        assertThat(shoppingCartService.updateItemQuantity(USER_ID, SKU, BigDecimal.ZERO)).isNull();

        verify(shoppingCartPort).deleteByUserIdAndSku(USER_ID, SKU);
        verify(shoppingCartPort, never()).save(any(), any());
    }

    @Test
    void updateItemQuantity_throwsWhenItemDoesNotExist() {
        when(shoppingCartPort.findByUserIdAndSku(USER_ID, SKU)).thenReturn(Optional.empty());

        assertThrows(ShoppingCartNotFoundException.class,
            () -> shoppingCartService.updateItemQuantity(USER_ID, SKU, BigDecimal.ONE));
    }

    @Test
    void removeItemFromCart_deletesExistingItem() {
        when(shoppingCartPort.findByUserIdAndSku(USER_ID, SKU)).thenReturn(Optional.of(ITEM));

        shoppingCartService.removeItemFromCart(USER_ID, SKU);

        verify(shoppingCartPort).deleteByUserIdAndSku(USER_ID, SKU);
    }

    @Test
    void clearCart_deletesAllItemsForUser() {
        shoppingCartService.clearCart(USER_ID);

        verify(shoppingCartPort).deleteByUserId(USER_ID);
    }
}
