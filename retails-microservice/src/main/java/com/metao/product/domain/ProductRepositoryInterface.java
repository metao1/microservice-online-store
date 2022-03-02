package com.metao.product.domain;

import com.metao.product.application.exception.ProductNotFoundException;
import com.metao.product.infrustructure.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryInterface extends BaseRepository<ProductEntity> {

    Optional<ProductEntity> findProductEntityById(ProductId productId) throws ProductNotFoundException;

    Optional<List<ProductEntity>> findAllProductEntitiesWithOffset(int limit, int offset);
}

