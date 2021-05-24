package com.metao.product.checkout.repository;

import com.metao.product.checkout.domain.ProductInventoryEntity;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    @NonNull Optional<ProductInventoryEntity> findById(@NonNull String productId);
}
