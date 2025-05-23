package com.metao.book.product.infrastructure.repository;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
                select distinct p, pc
                    from product p
                    left join fetch p.categories pc
                         where p.asin = :asin
            """)
    Optional<Product> findByAsin(@Param("asin") String asin);

    @Query("""
                select distinct p, pc
                    from product p
                    left join p.categories pc
                         where pc.category = :category
            """)
    List<Product> findAllByCategories(@Param("category") String category, Pageable pageable);

    @Query("SELECT pc FROM product_category pc WHERE pc.category = :category")
    Optional<ProductCategory> findByCategory(@Param("category") String category);

    @Query("SELECT pc FROM product_category pc WHERE pc.category IN :categories")
    List<ProductCategory> findByCategoryIn(@Param("categories") List<String> categories);

    @Query(value = """
            SELECT * FROM product_table
            WHERE to_tsvector('english', description) @@ plainto_tsquery('english', :query)
            """, nativeQuery = true)
    List<Product> searchDescriptions(@Param("query") String query);
}
