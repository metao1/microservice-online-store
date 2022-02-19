package com.metao.product.retails.domain.category;

import com.metao.product.retails.domain.product.ProductCategoryEntity;
import com.metao.product.retails.domain.product.ProductEntity;
import com.metao.product.retails.domain.product.ProductId;
import com.metao.product.retails.infrustructure.BaseRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends BaseRepository<ProductEntity> {

    Optional<ProductCategoryEntity> findProductCategoryByProductId(ProductId productId);

}

