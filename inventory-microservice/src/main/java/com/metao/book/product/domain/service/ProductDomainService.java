package com.metao.book.product.domain.service;

import com.metao.book.product.domain.exception.CategoryNotFoundException;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductId;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Domain service for complex business operations involving products
 */
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Check if a product can be assigned to a category
     */
    public boolean canAssignToCategory(@NonNull ProductId productId, @NonNull CategoryName categoryName) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        // Verify category exists and check if product can have more categories
        categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        // Business rule: A product can only be in maximum 5 categories
        return product.getCategories().size() < 5;
    }

    /**
     * Assign product to category with business rules validation
     */
    public void assignProductToCategory(@NonNull ProductId productId, @NonNull CategoryName categoryName) {
        if (!canAssignToCategory(productId, categoryName)) {
            throw new IllegalStateException("Product cannot be assigned to more than 5 categories");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductCategory category = categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        product.addCategory(category);
        productRepository.save(product);
    }

    /**
     * Check if a product is unique by ASIN
     */
    public boolean isProductUnique(@NonNull String asin) {
        return productRepository.findByAsin(asin).isEmpty();
    }

    /**
     * Find related products by shared categories
     */
    public List<Product> findRelatedProducts(@NonNull ProductId productId, int limit) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getCategories().isEmpty()) {
            return List.of();
        }

        // Get category names from the product
        List<CategoryName> categoryNames = product.getCategories().stream()
            .map(ProductCategory::getName)
            .toList();

        // Find products in same categories, excluding the original product
        return productRepository.findByCategories(categoryNames, 0, limit)
            .stream()
            .filter(p -> !p.getId().equals(productId))
            .toList();
    }

    /**
     * Check if a category can be deleted (no products assigned)
     */
    public boolean canDeleteCategory(@NonNull CategoryName categoryName) {
        List<Product> productsInCategory = productRepository.findByCategory(categoryName, 0, 1);
        return productsInCategory.isEmpty();
    }
}
