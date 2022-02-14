package com.metao.product.retails.domain.product;

import com.metao.product.retails.infrustructure.BaseRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends BaseRepository<ProductEntity, ProductId> {

    Optional<ProductEntity> findProductEntityById(ProductId productId);

    List<ProductEntity> findAllProductsWithOffset(ProductId productId, Pageable pageable);

    List<ProductEntity> findAllProductsWithCategoryAndOffset(ProductId productId, String category, Pageable pageable);

}

