package com.metao.product.checkout.repository;

import com.metao.product.checkout.domain.ProductInventoryEntity;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    @NotNull Optional<ProductInventoryEntity> findById(@NotNull String productId);
}
