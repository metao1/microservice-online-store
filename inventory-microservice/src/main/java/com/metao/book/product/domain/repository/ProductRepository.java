package com.metao.book.product.domain.repository;

import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Product aggregate
 */
public interface ProductRepository {

    /**
     * Save a product
     */
    void save(Product product);

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(ProductSku productSku);

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
    boolean existsById(ProductSku productSku);

    /**
     * Delete product
     */
    void delete(Product product);

    /**
     * Find all products with pagination
     */
    List<Product> findAll(int offset, int limit);
}
