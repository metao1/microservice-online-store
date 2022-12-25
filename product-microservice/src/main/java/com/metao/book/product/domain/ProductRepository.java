package com.metao.book.product.domain;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, ProductId> {

    @Query("""
                select distinct p, pc
                    from ProductEntity p
                    join fetch p.categories pc
                         where p.id = :productId
            """)
    Optional<Set<ProductEntity>> findProductCategoriesByProductId(@Param("productId") ProductId productId);

    Optional<ProductEntity> findByAsin(String asin);

}
