package com.metao.product.card.repository;

import com.metao.product.card.domain.ShoppingCard;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ShoppingCardRepository extends CrudRepository<ShoppingCard, String> {

    @Modifying
    @Transactional
    @Query("UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ?1 AND asin =?2")
    int updateQuantityForShoppingCard(String userId, String asin);

    @Query("SELECT quantity FROM shopping_cart WHERE user_id = ?1 AND asin = ?2")
    Optional<Integer> findByUserIdAndAsin(String userId, String asin);

    @Query("SELECT sc FROM shopping_cart sc WHERE sc.userId = ?1")
    Optional<List<ShoppingCard>> findProductsInCardByUserId(String userId);

    @Modifying
    @Transactional
    @Query("UPDATE shopping_cart SET quantity = quantity - 1 WHERE user_id = ?1 AND asin =?2")
    int decrementQuantityForShoppingCard(String userId, String asin);

    @Modifying
    @Transactional
    @Query("DELETE FROM shopping_cart WHERE user_id = ?1")
    int deleteProductsInCardByUserId(String userId);
}
