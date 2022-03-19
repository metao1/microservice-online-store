package com.metao.book.retails.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

import com.metao.book.retails.domain.*;

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
