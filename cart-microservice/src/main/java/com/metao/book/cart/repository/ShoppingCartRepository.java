package com.metao.book.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.metao.book.cart.domain.ShoppingCart;
import com.metao.book.cart.domain.ShoppingCartKey;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, ShoppingCartKey> {

    @Query("SELECT sc FROM #{#entityName} sc WHERE sc.userId=:userId")
    List<ShoppingCart> findProductsInCartByUserId(@Param("userId") String userId);

    @Query("SELECT sc FROM #{#entityName} sc WHERE sc.userId=:#{#key.userId} AND sc.asin=:#{#key.asin}")
    Optional<ShoppingCart> findByUserIdAndAsin(@Param("key") ShoppingCartKey shoppingCartKey);
}
