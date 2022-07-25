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
        shoppingCartService.addProductToShoppingCart("userId", "asin");
        var productsInCart = shoppingCartService.getProductsInCartByUserId("userId");
        assertTrue(productsInCart.containsKey("userId"));
        var shoppingList = List.of(ShoppingCart.createCart(new ShoppingCartKey("userId", "asin")));
        assertThat(productsInCart
                .get("userId"))
                .isEqualTo(shoppingList)
                .hasSize(1)
                .contains(ShoppingCart.createCart(new ShoppingCartKey("userId", "asin")));
    }

    @Test
    void getProductsInCart() {
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