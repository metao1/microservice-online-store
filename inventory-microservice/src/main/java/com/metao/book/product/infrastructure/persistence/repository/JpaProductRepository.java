package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for ProductEntity
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, ProductSku> {

    Optional<ProductEntity> findBySku(ProductSku sku);

    @Query("SELECT p FROM product p JOIN p.categories c WHERE c.category = :categoryName")
    List<ProductEntity> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT p FROM product p JOIN p.categories c WHERE c.category IN :categoryNames")
    List<ProductEntity> findByCategories(@Param("categoryNames") List<String> categoryNames, Pageable pageable);

    @Query("SELECT p FROM product p WHERE " +
        "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProductEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsBySku(ProductSku sku);
}
