package com.metao.book.checkout.domain;

import com.metao.book.checkout.domain.ProductInventoryEntity;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
    @NotNull
    Optional<ProductInventoryEntity> findByAsin(@NotNull String asin);
}
