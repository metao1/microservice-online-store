package com.metao.book.checkout.domain;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {

    @NotNull
    Optional<ProductInventoryEntity> findByAsin(@NotNull String asin);
}
