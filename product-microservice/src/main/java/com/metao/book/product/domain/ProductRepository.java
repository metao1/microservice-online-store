package com.metao.book.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, ProductId> {

    @Query("select p from ProductEntity p where p.id = ?1")
    Optional<Integer> findByProductCount(ProductId productId);


}
