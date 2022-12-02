package com.metao.book.checkout.repository;

import com.metao.book.checkout.domain.ProductInventoryEntity;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    @NotNull
    Optional<ProductInventoryEntity> findByAsin(@NotNull String asin);
}
