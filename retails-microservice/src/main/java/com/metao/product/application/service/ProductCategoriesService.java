package com.metao.product.application.service;

import com.metao.product.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductCategoriesService implements ProductCategoriesInterface {

    private final ProductRepository productRepository;

    @Override
    public Set<ProductCategoryEntity> getProductCategories(ProductId productId) {
        return productRepository
                .findProductEntityById(productId)
                .stream()
                .map(ProductEntity::getProductCategory)
                .findAny()
                .orElseThrow();
    }
}
