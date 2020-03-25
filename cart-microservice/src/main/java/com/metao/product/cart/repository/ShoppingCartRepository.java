package com.metao.product.cart.repository;

import com.metao.product.cart.domain.ShoppingCart;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, String> {

    @Modifying
    @Transactional
    @Query("UPDATE ShoppingCart SET quantity = quantity + 1 WHERE userId=:userId AND asin=:asin")
    int updateQuantityForShoppingCart(String userId, String asin);

    @Query("SELECT quantity FROM ShoppingCart WHERE userId=:userId AND asin=:asin")
    Optional<Integer> findByUserIdAndAsin(String userId, String asin);

    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.userId=:userId")
    Optional<List<ShoppingCart>> findProductsInCartByUserId(String userId);

    @Modifying
    @Transactional
    @Query("UPDATE ShoppingCart SET quantity = quantity - 1 WHERE userId=:userId AND asin=:asin")
    int decrementQuantityForShoppingCart(String userId, String asin);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShoppingCart WHERE userId=:userId")
    int deleteProductsInCartByUserId(String userId);
}
