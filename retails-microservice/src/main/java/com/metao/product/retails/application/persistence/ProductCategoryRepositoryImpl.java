package com.metao.product.retails.application.persistence;

import com.metao.product.retails.domain.category.ProductCategoryRepository;
import com.metao.product.retails.domain.product.ProductCategoryEntity;
import com.metao.product.retails.domain.product.ProductEntity;
import com.metao.product.retails.domain.product.ProductId;
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
