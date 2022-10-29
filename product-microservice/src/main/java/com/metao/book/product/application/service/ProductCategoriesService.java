package com.metao.book.product.application.service;

import com.metao.book.product.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductCategoriesService implements ProductCategoriesInterface {

    private final ProductRepository productRepository;

    @Override
    public Set<ProductCategoryEntity> getProductCategories(String productId) {
        return productRepository
                .findById(new ProductId(productId))
                .stream()
                .map(ProductEntity::getProductCategory)
                .findAny()
                .orElseThrow();
    }
}
