package com.metao.book.cart.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.util.BasePostgresIntegrationTest;
import com.metao.book.shared.application.service.order.OrderEventValidator;
import com.metao.book.shared.domain.financial.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(value = OrderEventValidator.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartServiceIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {
        this.shoppingCart = ShoppingCart.createCart(ConstantsTest.USER_ID,
            ConstantsTest.ASIN,
            ConstantsTest.DEFAULT_PRICE,
            ConstantsTest.DEFAULT_PRICE,
            ConstantsTest.DEFAULT_QUANTITY,
            Currency.EUR);
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
