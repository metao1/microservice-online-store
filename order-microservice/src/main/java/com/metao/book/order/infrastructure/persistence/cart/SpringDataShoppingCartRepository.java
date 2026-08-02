package com.metao.book.order.infrastructure.persistence.cart;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataShoppingCartRepository extends JpaRepository<ShoppingCartJpaEntity, ShoppingCartJpaKey> {

    Optional<ShoppingCartJpaEntity> findByUserIdAndSku(String userId, String sku);

    List<ShoppingCartJpaEntity> findByUserId(String userId);

    List<ShoppingCartJpaEntity> findByUserIdAndSkuIn(String userId, Set<String> skus);

    void deleteByUserIdAndSku(String userId, String sku);

    void deleteByUserId(String userId);
}
