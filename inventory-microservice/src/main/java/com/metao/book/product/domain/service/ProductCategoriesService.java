package com.metao.book.product.domain.service;

import com.metao.book.product.domain.category.ProductCategoriesInterface;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.ProductId;
import com.metao.book.product.domain.repository.ProductRepository;
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
    public Set<ProductCategory> getProductCategories(String id) {
        ProductId productId = ProductId.of(id);
        return productRepository.findById(productId)
            .map(Product::getCategories)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
