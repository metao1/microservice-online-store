package com.metao.product.checkout.repository;

import com.metao.product.checkout.domain.ProductInventoryEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    Optional<ProductInventoryEntity> findById(String productId);
}
