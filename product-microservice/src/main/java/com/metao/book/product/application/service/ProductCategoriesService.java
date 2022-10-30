package com.metao.book.product.application.service;

import com.metao.book.product.domain.ProductCategoriesInterface;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCategoriesService implements ProductCategoriesInterface {

    private final ProductRepository productRepository;

    @Override
    public Optional<Set<ProductCategoryEntity>> getProductCategories(String productId) {
        return Optional.of(productRepository
            .findProductCategoriesByProductId(new ProductId(productId))
            .orElseThrow());
    }
}
