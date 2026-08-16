package com.metao.book.order.infrastructure.persistence.cart;

import com.metao.book.order.application.cart.ShoppingCartItem;
import com.metao.book.order.application.port.ShoppingCartPort;
import com.metao.book.shared.architecture.OutboundAdapter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@OutboundAdapter
@RequiredArgsConstructor
public class ShoppingCartPersistenceAdapter implements ShoppingCartPort {

    private final ShoppingCartRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingCartItem> findByUserId(String userId) {
        return repository.findByUserId(userId).stream().map(this::toItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingCartItem> findByUserIdAndSkuIn(String userId, Set<String> skus) {
        return repository.findByUserIdAndSkuIn(userId, skus).stream().map(this::toItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCartItem> findByUserIdAndSku(String userId, String sku) {
        return repository.findByUserIdAndSku(userId, sku).map(this::toItem);
    }

    @Override
    @Transactional
    public void saveAll(String userId, Collection<ShoppingCartItem> items) {
        repository.saveAll(items.stream().map(item -> toEntity(userId, item)).toList());
    }

    @Override
    @Transactional
    public ShoppingCartItem save(String userId, ShoppingCartItem item) {
        return toItem(repository.save(toEntity(userId, item)));
    }

    @Override
    @Transactional
    public void deleteByUserIdAndSku(String userId, String sku) {
        repository.deleteByUserIdAndSku(userId, sku);
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        repository.deleteByUserId(userId);
    }

    private ShoppingCartItem toItem(ShoppingCartJpaEntity entity) {
        return new ShoppingCartItem(entity.getSku(), entity.getProductTitle(), entity.getQuantity(), entity.getSellPrice(), entity.getCurrency());
    }

    private ShoppingCartJpaEntity toEntity(String userId, ShoppingCartItem item) {
        ShoppingCartJpaEntity entity = new ShoppingCartJpaEntity();
        entity.setUserId(userId);
        entity.setSku(item.sku());
        entity.setProductTitle(item.productTitle());
        entity.setQuantity(item.quantity());
        entity.setSellPrice(item.price());
        entity.setBuyPrice(item.price());
        entity.setCurrency(item.currency());
        entity.setUpdatedOn(System.currentTimeMillis());
        return entity;
    }
}
