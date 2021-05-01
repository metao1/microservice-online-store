package com.metao.product.checkout.repository;

import com.metao.product.checkout.domain.ProductInventoryEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    @NotNull Optional<ProductInventoryEntity> findById(@NotNull String productId);
}
