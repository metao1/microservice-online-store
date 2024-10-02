package com.metao.book.product.infrastructure.repository;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, ProductId>,
    JpaRepository<ProductEntity, ProductId> {

    @Query("""
            select distinct p, pc
                from product p
                join fetch p.categories pc
                     where p.asin = :productId
        """)
    Optional<ProductEntity> findByProductId(@Param("productId") String productId);

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

}
