package com.metao.book.cart.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
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
        var shoppingCart = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin"));
        shoppingCartRepository.save(shoppingCart);
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey("userId", "asin"))).isPresent();
    }

    @Test
    void updateQuantityForShoppingCart() {
        var key = new ShoppingCartKey("userId", "asin");
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(key);
        shoppingCartRepository.save(currentShoppingCart);
        currentShoppingCart.setQuantity(2);
        shoppingCartRepository.save(currentShoppingCart);
        assertThat(shoppingCartRepository.findByUserIdAndAsin(key).get().getQuantity()).isEqualTo(2);
    }

    @Test
    void findProductsInCartByUserId() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin"));
        shoppingCartRepository.save(currentShoppingCart);
        var shoppingCartKey = new ShoppingCartKey("userId", "asin");
        var id = shoppingCartRepository.findByUserIdAndAsin(shoppingCartKey);
        assertThat(id).isPresent();
        assertThat(shoppingCartRepository.findProductsInCartByUserId("userId")).isNotNull()
            .matches(shoppingCart -> shoppingCart.size() == 1)
            .matches(shoppingCart -> shoppingCart.get(0).getAsin().equals("asin"))
            .matches(shoppingCart -> shoppingCart.get(0).getUserId().equals("userId"))
            .matches(shoppingCart -> shoppingCart.get(0).getQuantity() == 1);
    }

    @Test
    void decrementQuantityForShoppingCart() {
        var key = new ShoppingCartKey("userId", "asin");
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(key);
        shoppingCartRepository.save(currentShoppingCart);
        currentShoppingCart.setQuantity(0);
        shoppingCartRepository.save(currentShoppingCart);
        assertThat(shoppingCartRepository.findById(key).get().getQuantity()).isEqualTo(0);
    }

    @Test
    void deleteProductsInCartByUserId() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin"));
        shoppingCartRepository.save(currentShoppingCart);
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey("userId", "asin"))).isPresent();
        shoppingCartRepository.delete(currentShoppingCart);
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey("userId", "asin"))).isEmpty();
    }

    @Test
    void productsInCartByUserId_NotFound() {
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey("userId", "asin"))).isEmpty();
    }

    @Test
    void saveProductsInCartByUserId_FoundTwoProducts() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin1"));
        shoppingCartRepository.save(currentShoppingCart);
        ShoppingCart currentShoppingCart2 = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin2"));
        shoppingCartRepository.save(currentShoppingCart2);
        assertThat(shoppingCartRepository.findProductsInCartByUserId("userId")).satisfies(sc -> {
            assertThat(sc).isNotNull()
                .matches(shoppingCart -> shoppingCart.size() == 2)
                .matches(shoppingCart -> shoppingCart.get(0).getAsin().equals("asin1"))
                .matches(shoppingCart -> shoppingCart.get(0).getUserId().equals("userId"))
                .matches(shoppingCart -> shoppingCart.get(0).getQuantity() == 1)
                .matches(shoppingCart -> shoppingCart.get(1).getAsin().equals("asin2"))
                .matches(shoppingCart -> shoppingCart.get(1).getUserId().equals("userId"))
                .matches(shoppingCart -> shoppingCart.get(1).getQuantity() == 1);
        });
    }

    @Test
    void saveProductsInCartByUserId_FoundOneProduct() {
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin1"));
        shoppingCartRepository.save(currentShoppingCart);
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey("userId", "asin1"))
            .get().getQuantity())
            .isEqualTo(1);
    }

}