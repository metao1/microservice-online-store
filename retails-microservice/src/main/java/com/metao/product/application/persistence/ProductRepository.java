package com.metao.product.application.persistence;

import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.ProductRepositoryInterface;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
public class ProductRepository implements ProductRepositoryInterface {
    private Map<ProductId, ProductEntity> map = new ConcurrentHashMap<>();

    @Override
    public void save(ProductEntity productEntity) {
        log.info("product {} saved.", productEntity.id());
        map.put(productEntity.id(), productEntity);
    }

    @Override
    public Optional<ProductEntity> findProductEntityById(@NotNull ProductId productId) {
        return Optional.ofNullable(map.get(productId));
    }
}
