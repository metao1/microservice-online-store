package com.metao.book.order.application.port;

import com.metao.book.order.application.cart.ShoppingCartItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShoppingCartPort {

    List<ShoppingCartItem> findByUserId(String userId);

    List<ShoppingCartItem> findByUserIdAndSkuIn(String userId, Set<String> skus);

    Optional<ShoppingCartItem> findByUserIdAndSku(String userId, String sku);

    void saveAll(String userId, Collection<ShoppingCartItem> items);

    ShoppingCartItem save(String userId, ShoppingCartItem item);

    void deleteByUserIdAndSku(String userId, String sku);

    void deleteByUserId(String userId);
}
