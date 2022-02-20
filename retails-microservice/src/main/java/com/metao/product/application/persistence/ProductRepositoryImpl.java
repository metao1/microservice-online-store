package com.metao.product.application.persistence;


import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("productRepository")
public class ProductRepositoryImpl implements ProductRepository  {

    @Override
    public void save(ProductEntity productEntity) {

    }

    @Override
    public Optional<ProductEntity> findProductEntityById(ProductId productId) {
        return Optional.empty();
    }
}
