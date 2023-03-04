package com.metao.book.cart.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.util.BasePostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartServiceIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private ShoppingCartService shoppingCartService;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        this.shoppingCart = ShoppingCart.createCart(ConstantsTest.USER_ID,
                ConstantsTest.ASIN,
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
    }

    @Test
    void addProductToShoppingCart() {
        shoppingCartService.addOrderToShoppingCart(shoppingCart);
        var productsInCart = shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID);
        assertThat(productsInCart)
                .hasSize(1)
                .containsExactlyInAnyOrder(shoppingCart);

    }

    @Test
    void getProductsInCart() {
        shoppingCartService.addOrderToShoppingCart(shoppingCart);
        assertThat(shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID))
                .extracting(ConstantsTest.USER_ID)
                .isNotNull();
    }
}
