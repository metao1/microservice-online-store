package com.metao.book.retails.application.service;

import com.metao.book.retails.domain.*;
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
                .findById(productId)
                .stream()
                .map(ProductEntity::getProductCategory)
                .findAny()
                .orElseThrow();
    }
}
