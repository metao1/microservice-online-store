package com.metao.book.cart.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import com.metao.book.cart.util.BasePostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartServiceIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Test
    void addProductToShoppingCart() {
        shoppingCartService.addProductToShoppingCart(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        var productsInCart = shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID);
        var shoppingCartKey = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(shoppingCartKey);
        assertThat(productsInCart)
            .hasSize(1)
            .containsExactlyInAnyOrder(currentShoppingCart);

    }

    @Test
    void getProductsInCart() {
        shoppingCartService.addProductToShoppingCart(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        assertThat(shoppingCartService.getProductsInCartByUserId(ConstantsTest.USER_ID))
            .extracting(ConstantsTest.USER_ID)
            .isNotNull();
    }
}
