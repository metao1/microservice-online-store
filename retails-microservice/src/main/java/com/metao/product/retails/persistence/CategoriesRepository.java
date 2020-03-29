package com.metao.product.retails.persistence;

import com.metao.product.retails.domain.ProductCategoryEntity;
import com.metao.product.retails.domain.ProductEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepository extends CrudRepository<ProductEntity, String> {

    @Query("select u from ProductCategoryEntity u")
    List<ProductCategoryEntity> findProductEntities();
}
