package com.metao.book.product.domain.service;

import com.metao.book.product.domain.exception.CategoryNotFoundException;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Domain service for complex business operations involving products
 */
@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Check if a product can be assigned to a category
     */
    public boolean canAssignToCategory(@NonNull ProductSku productSku, @NonNull CategoryName categoryName) {
        Product product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        // Verify category exists and check if product can have more categories
        categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        // Business rule: A product can only be in maximum 5 categories
        return product.getCategories().size() < 5;
    }

    /**
     * Assign product to category with business rules validation
     */
    public void assignProductToCategory(@NonNull ProductSku productSku, @NonNull CategoryName categoryName) {
        if (!canAssignToCategory(productSku, categoryName)) {
            throw new IllegalStateException("Product cannot be assigned to more than 5 categories");
        }

        Product product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        ProductCategory category = categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        product.addCategory(category);
        productRepository.save(product);
    }

    /**
     * Check if a product is unique by SKU
     */
    public Boolean isProductUnique(@NotEmpty String sku) {
        ProductSku productSku = ProductSku.of(sku);
        return productRepository.findBySku(productSku).isEmpty();
    }

    /**
     * Find related products by shared categories
     */
    public List<Product> findRelatedProducts(@NonNull ProductSku productSku, int limit) {
        Product product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

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
            .filter(p -> !p.getId().equals(productSku))
            .toList();
    }
}
