package com.metao.book.cart.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import com.metao.book.cart.service.ConstantsTest;
import java.util.Optional;
import java.util.Set;
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

    @Test
    void saveShoppingCart() {
        var shoppingCart = ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN));
        shoppingCartRepository.save(shoppingCart);
        assertThat(shoppingCartRepository.findById(
            new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN))).isPresent();
    }

    @Test
    void updateQuantityForShoppingCart() {
        var key = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(key);
        shoppingCartRepository.save(currentShoppingCart);
        currentShoppingCart.setQuantity(2);
        shoppingCartRepository.save(currentShoppingCart);
        final var shoppingCart = shoppingCartRepository.findByUserIdAndAsin(key).orElseThrow();
        assertThat(shoppingCart.getQuantity()).isEqualTo(2);
    }

    @Test
    void findProductsInCartByUserId() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(
            new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN));
        shoppingCartRepository.save(currentShoppingCart);
        var shoppingCartKey = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        var id = shoppingCartRepository.findByUserIdAndAsin(shoppingCartKey);
        assertThat(id).isPresent();
        var expectedShoppingCart = Set.of(
            ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)));
        assertThat(shoppingCartRepository.findProductsInCartByUserId(ConstantsTest.USER_ID))
            .isNotNull()
            .isEqualTo(expectedShoppingCart);
    }

    @Test
    void decrementQuantityForShoppingCart() {
        var key = new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN);
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(key);
        shoppingCartRepository.save(currentShoppingCart);
        currentShoppingCart.setQuantity(0);
        shoppingCartRepository.save(currentShoppingCart);
        final var shoppingCart = shoppingCartRepository.findById(key).orElseThrow();
        assertThat(shoppingCart.getQuantity()).isEqualTo(0);
    }

    @Test
    void deleteProductsInCartByUserId() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(
            new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN));
        shoppingCartRepository.save(currentShoppingCart);
        assertThat(shoppingCartRepository.findById(
            new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN))).isPresent();
        shoppingCartRepository.delete(currentShoppingCart);
        assertThat(
            shoppingCartRepository.findById(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN))).isEmpty();
    }

    @Test
    void productsInCartByUserId_NotFound() {
        assertThat(
            shoppingCartRepository.findById(new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN))).isEmpty();
    }

    @Test
    void saveProductsInCartByUserId_FoundTwoProducts() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(
            new ShoppingCartKey(ConstantsTest.USER_ID, "asin1"));
        shoppingCartRepository.save(currentShoppingCart);
        ShoppingCart currentShoppingCart2 = ShoppingCart.createCart(
            new ShoppingCartKey(ConstantsTest.USER_ID, "asin2"));
        shoppingCartRepository.save(currentShoppingCart2);
        var expectedShoppingCart = Set.of(
            ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, "asin1")),
            ShoppingCart.createCart(new ShoppingCartKey(ConstantsTest.USER_ID, "asin2"))
        );
        assertThat(shoppingCartRepository.findProductsInCartByUserId(ConstantsTest.USER_ID))
            .satisfies(sc -> assertThat(sc).isNotNull())
            .isEqualTo(expectedShoppingCart);
    }

    @Test
    void saveProductsInCartByUserId_FoundOneProduct() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(
            new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN));
        shoppingCartRepository.save(currentShoppingCart);
        final var shoppingCart = shoppingCartRepository.findById(
            new ShoppingCartKey(ConstantsTest.USER_ID, ConstantsTest.ASIN)).orElseThrow();
        assertThat(shoppingCart.getQuantity())
            .isEqualTo(1);
    }

}