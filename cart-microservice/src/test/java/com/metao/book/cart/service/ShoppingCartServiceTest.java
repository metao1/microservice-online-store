package com.metao.book.cart.service;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ShoppingCartServiceTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Test
    void addProductToShoppingCart() {
        shoppingCartService.addProductToShoppingCart(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        var productsInCart = shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID);
        assertTrue(productsInCart.containsKey(ConstantsTest.USER_ID));
        var shoppingList = List.of(ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)));
        assertThat(productsInCart
                .get(ConstantsTest.USER_ID))
                .isEqualTo(shoppingList)
                .hasSize(1)
                .contains(ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)));
    }

    @Test
    void getProductsInCart() {
        shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID);
    }

    @Test
    void removeProductFromCart() {
    }

    @Test
    void createCart() {
    }

    @Test
    void clearCart() {
    }
}