package com.metao.book.order.application.cart;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, ShoppingCartKey> {

    Optional<ShoppingCart> findByUserIdAndSku(String userId, String sku);

    List<ShoppingCart> findByUserId(String userId);

    List<ShoppingCart> findByUserIdAndSkuIn(String userId, Set<String> skus);

    void deleteByUserIdAndSku(String userId, String sku);

    void deleteByUserId(String userId);
}
