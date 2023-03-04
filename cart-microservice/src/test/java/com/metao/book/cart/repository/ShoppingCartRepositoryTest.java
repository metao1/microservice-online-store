package com.metao.book.cart.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import com.metao.book.cart.service.ConstantsTest;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("container")
@DataJpaTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ShoppingCartRepositoryTest {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
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
    void checkCartItemsAreEquals() {
        var shoppingCart1 = ShoppingCart.createCart("USER1",
                ConstantsTest.ASIN,
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
        var shoppingCart2 = ShoppingCart.createCart("USER2",
                ConstantsTest.ASIN,
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);

        assertNotEquals(shoppingCart1, shoppingCart2);

        var shoppingCart3 = ShoppingCart.createCart("USER1",
                ConstantsTest.ASIN,
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
        var shoppingCart4 = ShoppingCart.createCart("USER1",
                ConstantsTest.ASIN,
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
        assertEquals(shoppingCart3, shoppingCart4);

        var shoppingCart5 = ShoppingCart.createCart(ConstantsTest.USER_ID,
                "asin1",
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
        var shoppingCart6 = ShoppingCart.createCart(ConstantsTest.USER_ID,
                "asin2",
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);

        assertNotEquals(shoppingCart5, shoppingCart6);
    }

    @Test
    void saveShoppingCart() {
        shoppingCartRepository.save(shoppingCart);
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)))
                .isPresent();
    }

    @Test
    void updateQuantityForShoppingCart() {
        var key = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        shoppingCartRepository.save(shoppingCart);
        shoppingCart.increaseQuantity();
        shoppingCart.increaseQuantity();
        shoppingCartRepository.save(shoppingCart);
        final var shoppingCart = shoppingCartRepository.findByUserIdAndAsin(key).orElseThrow();
        assertThat(shoppingCart.getQuantity()).isEqualTo(BigDecimal.valueOf(3));
    }

    @Test
    void findProductsInCartByUserId() {
        shoppingCartRepository.save(shoppingCart);
        var shoppingCartKey = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        var id = shoppingCartRepository.findByUserIdAndAsin(shoppingCartKey);
        assertThat(id).isPresent();
        var expectedShoppingCart = Set.of(shoppingCart);
        assertThat(shoppingCartRepository.findProductsInCartByUserId(ConstantsTest.USER_ID))
                .isNotNull()
                .isEqualTo(expectedShoppingCart);
    }

    @Test
    void decrementQuantityForShoppingCart() {
        var key = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        shoppingCartRepository.save(shoppingCart);
        shoppingCart.decreaseQuantity();
        shoppingCartRepository.save(shoppingCart);
        final var shoppingCart = shoppingCartRepository.findById(key).orElseThrow();
        assertThat(shoppingCart.getQuantity()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void deleteProductsInCartByUserId() {
        shoppingCartRepository.save(shoppingCart);
        assertThat(shoppingCartRepository.findById(
                new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN))).isPresent();
        shoppingCartRepository.delete(shoppingCart);
        assertThat(
                shoppingCartRepository.findById(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)))
                .isEmpty();
    }

    @Test
    void productsInCartByUserId_NotFound() {
        assertThat(
                shoppingCartRepository.findById(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)))
                .isEmpty();
    }

    @Test
    void saveProductsInCartByUserId_FoundTwoProducts() {
        var shoppingCart1 = ShoppingCart.createCart(ConstantsTest.USER_ID,
                ConstantsTest.ASIN,
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
        var shoppingCart2 = ShoppingCart.createCart(ConstantsTest.USER_ID,
                "asin2",
                ConstantsTest.PRICE,
                ConstantsTest.PRICE,
                ConstantsTest.QUANTITY);
        var cards = Set.of(shoppingCart1, shoppingCart2);
        shoppingCartRepository.saveAll(cards);
        assertThat(shoppingCartRepository.findProductsInCartByUserId(ConstantsTest.USER_ID))
                .satisfies(sc -> assertThat(sc).isNotNull())
                .isEqualTo(cards);
    }

    @Test
    void saveProductsInCartByUserId_FoundOneProduct() {
        shoppingCartRepository.save(shoppingCart);
        final var shoppingCart = shoppingCartRepository.findById(
                new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)).orElseThrow();
        assertThat(shoppingCart.getQuantity())
                .isEqualTo(ConstantsTest.QUANTITY);
    }

}