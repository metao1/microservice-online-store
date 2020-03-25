package com.metao.product.checkout.repository;

import com.metao.product.checkout.domain.ProductInventoryEntity;
import org.springframework.data.repository.CrudRepository;

public interface ProductInventoryRepository extends CrudRepository<ProductInventoryEntity, String> {
}
