package com.metao.product.domain;

import com.metao.product.infrustructure.BaseRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends BaseRepository<ProductEntity> {

    Optional<ProductCategoryEntity> findProductCategoryByProductId(ProductId productId);

}

