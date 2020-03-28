package com.metao.product.retails.persistence;

import com.metao.product.retails.domain.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<ProductEntity, String> {

    @Query(value = "SELECT distinct u FROM ProductEntity u join fetch u.categories  where u.id =:productId")
    Optional<ProductEntity> findProductEntityById(String productId);

    @Transactional
    @Query(value = "SELECT u FROM ProductEntity u")
    List<ProductEntity> findAllProductsWithOffset(Pageable pageable);

    @Transactional
    @Query("SELECT distinct product FROM ProductEntity product left join fetch product.categories categories where categories IN :category")
    List<ProductEntity> findAllProductsWithCategoryAndOffset(@Param("category") String category, Pageable pageable);

}
