package com.metao.product.application.persistence;

import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductCategoryRepository;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("productCategoryRepository")
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    @Override
    public Optional<ProductCategoryEntity> findProductCategoryByProductId(ProductId productId) {
        return Optional.empty();
    }

    @Override
    public void save(ProductEntity productEntity) {

    }
}
