package com.metao.book.product.domain.repository;

import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductId;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Product aggregate
 */
public interface ProductRepository {

    /**
     * Save a product
     */
    Product save(Product product);

    /**
     * Find product by ID
     */
    Optional<Product> findById(ProductId productId);

    /**
     * Find product by ASIN (ProductId value)
     */
    Optional<Product> findByAsin(String asin);

    /**
     * Find products by category
     */
    List<Product> findByCategory(CategoryName categoryName, int offset, int limit);

    /**
     * Find products by multiple categories
     */
    List<Product> findByCategories(List<CategoryName> categoryNames, int offset, int limit);

    /**
     * Search products by keyword
     */
    List<Product> searchByKeyword(String keyword, int offset, int limit);

    /**
     * Check if product exists
     */
    boolean existsById(ProductId productId);

    /**
     * Delete product
     */
    void delete(Product product);

    /**
     * Find all products with pagination
     */
    List<Product> findAll(int offset, int limit);

    /**
     * Count total products
     */
    long count();
}
