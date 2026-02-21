package com.metao.book.product.domain.repository;

import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.product.ProductSku;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Product aggregate
 */
public interface ProductRepository {

    /**
     * Save a product
     */
    void save(ProductAggregate product);

    /**
     * Insert product if it does not already exist.
     *
     * @return true if inserted, false when product already exists
     */
    boolean insertIfAbsent(ProductAggregate product);

    /**
     * Find product by SKU
     */
    Optional<ProductAggregate> findBySku(ProductSku productSku);

    /**
     * Find products by SKUs
     */
    List<ProductAggregate> findBySkus(List<ProductSku> productSkus);

    /**
     * Find products by category
     */
    List<ProductAggregate> findByCategory(CategoryName categoryName, int offset, int limit);

    /**
     * Find products by multiple categories
     */
    List<ProductAggregate> findByCategories(List<CategoryName> categoryNames, int offset, int limit);

    /**
     * Search products by keyword
     */
    List<ProductAggregate> searchByKeyword(String keyword, int offset, int limit);

    /**
     * Check if product exists
     */
    boolean existsById(ProductSku productSku);

    /**
     * Delete product
     */
    void delete(ProductAggregate product);

    boolean reduceVolumeAtomically(ProductSku sku, BigDecimal quantity);

    void flush();
}
