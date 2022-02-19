package com.metao.product.retails.application.persistence;


import com.metao.product.retails.domain.product.ProductEntity;
import com.metao.product.retails.domain.product.ProductId;
import com.metao.product.retails.domain.product.ProductRepository;
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
