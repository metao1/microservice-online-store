package com.metao.book.cart.repository;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, ShoppingCartKey> {

    @Modifying
    @Transactional
    @Query("UPDATE ShoppingCart sc SET sc.quantity = sc.quantity + 1 WHERE sc.userId=:userId AND sc.asin=:asin")
    int updateQuantityForShoppingCart(@Param("userId") String userId, @Param("asin") String asin);

    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.userId=:userId")
    List<ShoppingCart> findProductsInCartByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE ShoppingCart sc SET sc.quantity = sc.quantity - 1 WHERE sc.userId=:userId AND sc.asin=:asin")
    int decrementQuantityForShoppingCart(@Param("userId") String userId, @Param("asin") String asin);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShoppingCart sc WHERE sc.userId=:userId")
    int deleteProductsInCartByUserId(@Param("userId") String userId);
}
