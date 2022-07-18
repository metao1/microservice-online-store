package com.metao.book.cart.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ShoppingCartServiceTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Test
    void addProductToShoppingCart() {
        shoppingCartService.addProductToShoppingCart("userId", "asin");

        var productsInCart = shoppingCartService.getProductsInCartByUserId("userId");
        assertTrue(productsInCart.containsKey("userId"));
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