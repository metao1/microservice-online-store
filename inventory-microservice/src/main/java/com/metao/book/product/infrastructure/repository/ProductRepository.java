package com.metao.book.product.infrastructure.repository;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
                select distinct p, pc
                    from product p
                    left join fetch p.categories pc
                         where p.asin = :asin
            """)
    Optional<Product> findByAsin(@Param("asin") String asin);

    @Query("""
                SELECT DISTINCT p
                FROM product p
                LEFT JOIN FETCH p.categories pc
                WHERE pc.category IN :categories
            """)
    List<Product> findAllByCategories(@Param("categories") List<String> categories, Pageable pageable);

    @Query("SELECT pc FROM product_category pc WHERE pc.category = :category")
    Optional<ProductCategory> findByCategory(@Param("category") String category);

    @Query("SELECT pc FROM product_category pc WHERE pc.category IN :categories")
    List<ProductCategory> findByCategoryIn(@Param("categories") List<String> categories);

    @Query(value = """
            SELECT * FROM product_table
            WHERE to_tsvector('english', description) @@ plainto_tsquery('english', :query)
            """, nativeQuery = true)
    List<Product> searchDescriptions(@Param("query") String query);

    @Query("SELECT p FROM product p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.asin) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    // Step 1: page on IDs only
    @Query("""
            SELECT p.id
            FROM product p
            LEFT JOIN FETCH p.categories c
            WHERE c.category = :category
            ORDER BY p.id
            """)
    List<Long> findProductIdsByCategory(
        @Param("category") String category,
        Pageable pageable
    );

    // Step 2: fetch the actual products *with* their categories eagerly
    @Query("""
              SELECT DISTINCT p
              FROM product p
              LEFT JOIN FETCH p.categories
              WHERE p.id IN :ids
            """)
    List<Product> findByIdInWithCategories(@Param("ids") List<Long> ids);
}
