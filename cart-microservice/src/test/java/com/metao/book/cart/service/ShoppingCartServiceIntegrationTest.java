package com.metao.book.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ShoppingCartServiceIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Test
    void addProductToShoppingCart() {
        shoppingCartService.addProductToShoppingCart(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        var productsInCart = shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID);
        assertTrue(productsInCart.containsKey(ConstantsTest.USER_ID));
        var shoppingList = List.of(
            ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)));
        assertThat(productsInCart
            .get(ConstantsTest.USER_ID))
            .isEqualTo(shoppingList)
            .hasSize(1)
            .contains(ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)));
    }

    @Test
    void getProductsInCart() {
        shoppingCartService.addProductToShoppingCart(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        assertThat(shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID))
            .extracting(Map::values)
            .isNotNull()
            .asList()
            .hasSize(1);
    }
}
