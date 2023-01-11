package com.metao.book.checkout.domain;

import java.util.Optional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    @NotNull
    Optional<ProductInventoryEntity> findByAsin(@NotNull String asin);
}
