package com.metao.product.retails.domain.product;

import com.metao.product.retails.infrustructure.BaseRepository;

import java.util.Optional;

public interface ProductRepository extends BaseRepository<ProductEntity> {

    Optional<ProductEntity> findProductEntityById(ProductId productId);

}

