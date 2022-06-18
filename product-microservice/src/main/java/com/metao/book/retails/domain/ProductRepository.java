package com.metao.book.retails.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("select p from ProductEntity p where p.id = ?1")
    Optional<Integer> findByProductCount(long productId);

}
