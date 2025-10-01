package com.metao.book.order.application.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, ShoppingCartKey> {

    Optional<ShoppingCart> findByUserIdAndAsin(String userId, String sku);

    List<ShoppingCart> findByUserId(String userId);

    void deleteByUserIdAndAsin(String userId, String sku);

    void deleteByUserId(String userId);
}
