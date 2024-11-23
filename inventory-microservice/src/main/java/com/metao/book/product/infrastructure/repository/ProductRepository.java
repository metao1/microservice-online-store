package com.metao.book.product.infrastructure.repository;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String>,
    JpaRepository<ProductEntity, String> {

    @Query("""
            select distinct p, pc
                from product p
                join fetch p.categories pc
                     where p.asin = :asin
        """)
    Optional<ProductEntity> findByAsin(@Param("asin") String asin);

    @Query("""
            select distinct p, pc
                from product p
                join fetch p.categories pc
                     where pc.category = :category
        """)
    List<ProductEntity> findAllByCategories(@Param("category") String category, Pageable pageable);

    @Query("SELECT pc FROM product_category pc WHERE pc.category = :category")
    Optional<ProductCategoryEntity> findByCategory(@Param("category") String category);

    @Query("SELECT pc FROM product_category pc WHERE pc.category IN :categories")
    List<ProductCategoryEntity> findByCategoryIn(@Param("categories") List<String> categories);

    @Transactional
    default void saveProduct(ProductEntity product) {
        Set<ProductCategoryEntity> resolvedCategories = new HashSet<>();
        for (ProductCategoryEntity categoryName : product.getCategories()) {
            var categories = findByCategory(categoryName.getCategory());
            if (categories.isPresent()) {
                product.setCategories(null);
            }
        }
        product.addCategories(resolvedCategories);
        save(product);
    }
}
