package com.metao.book.product.domain;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, ProductId> {

    @Query("""
            select distinct p, pc
                from product p
                join fetch p.categories pc
                     where p.asin = :productId
        """)
    Set<ProductEntity> findByProductId(@Param("productId") String productId);

    Optional<ProductEntity> findByAsin(String asin);

}
