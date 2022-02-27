package com.metao.product.application.persistence;

import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductCategoryRepositoryInterface;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductId;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductCategoryRepository implements ProductCategoryRepositoryInterface {

    @Override
    public Optional<ProductCategoryEntity> findProductCategoryByProductId(ProductId productId) {
        return Optional.empty();
    }

    @Override
    public void save(ProductEntity productEntity) {

    }
}
