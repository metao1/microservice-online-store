package com.metao.book.product.domain.service;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoriesInterface;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCategoriesService implements ProductCategoriesInterface {

    private final ProductRepository productRepository;

    @Override
    public Set<ProductCategoryEntity> getProductCategories(String productId) {
        return productRepository.findByAsin(productId).map(ProductEntity::getCategories)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
