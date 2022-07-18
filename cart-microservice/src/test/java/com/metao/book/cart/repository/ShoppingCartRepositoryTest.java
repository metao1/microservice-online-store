package com.metao.book.cart.repository;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
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
        ShoppingCart currentShoppingCart = ShoppingCart.createCart(new ShoppingCartKey("userId", "asin"));
        shoppingCartRepository.save(currentShoppingCart);
        shoppingCartRepository.updateQuantityForShoppingCart("userId", "new-asin");
        assertThat(shoppingCartRepository.findById(new ShoppingCartKey("userId", "new-asin")).get().getQuantity()).isEqualTo(1);
    }

    @Test
    void findProductsInCartByUserId() {
    }

    @Test
    void decrementQuantityForShoppingCart() {
    }

    @Test
    void deleteProductsInCartByUserId() {
    }
}