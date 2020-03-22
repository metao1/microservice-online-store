package com.metao.product.retails.persistence;

import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.model.ProductDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    Optional<ProductEntity> findProductEntityById(String productId);

        @Query(value = "SELECT u FROM ProductEntity u")
    List<ProductEntity> findAllProductsWithOffset(Pageable pageable);

    @Query("SELECT distinct u FROM ProductEntity u where u.categories IN :category")
    List<ProductDTO> findAllProductsWithCategoryAndOffset(@Param("category") String category, Pageable pageable);
}
