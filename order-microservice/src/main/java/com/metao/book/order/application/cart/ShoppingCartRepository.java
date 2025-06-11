package com.metao.book.order.application.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, ShoppingCartKey> {

    Optional<ShoppingCart> findByUserIdAndAsin(String userId, String asin);

    List<ShoppingCart> findByUserId(String userId);

    void deleteByUserIdAndAsin(String userId, String asin);

    void deleteByUserId(String userId);
}
