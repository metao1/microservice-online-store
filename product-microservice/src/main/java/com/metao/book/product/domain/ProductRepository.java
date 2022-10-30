package com.metao.book.product.domain;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, ProductId> {

    @Query("select p from ProductEntity p where p.id = ?1")
    Optional<Integer> findByProductCount(ProductId productId);

    @Query("select p from ProductEntity p join fetch p.productCategory pc on pc.category = :productId")
    Optional<Set<ProductCategoryEntity>> findProductEntitiesByProductId(@Param("productId") ProductId productId);

}
