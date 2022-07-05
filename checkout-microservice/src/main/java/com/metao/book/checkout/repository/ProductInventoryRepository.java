package com.metao.book.checkout.repository;

import com.metao.book.checkout.domain.ProductInventoryEntity;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, Long> {
    @NotNull
    Optional<ProductInventoryEntity> findById(@NotNull long customerId);
}
