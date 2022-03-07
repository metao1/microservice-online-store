package com.metao.product.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.ProductRepository;
import com.metao.product.infrustructure.repository.model.OffsetBasedPageRequest;
import com.metao.product.util.ProductTestUtils;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
class ProductRepositoryTest {

   @Autowired
   ProductRepository productRepository;

   @Test
   void saveProductEntity() {
       var pe = ProductTestUtils.createProductEntity();
       productRepository.save(pe); 
   }

   @Test
   void findProductEntityById_NotFound() {
       Optional<ProductEntity> entity = productRepository.findById(new ProductId("PRODUCT_ID"));
       assertTrue(entity.isEmpty());
   }

   @Test
   void findAllProductsWithOffset() {
        var pe = ProductTestUtils.createProductEntity();
       productRepository.save(pe);
       Pageable pageable = new OffsetBasedPageRequest(0, 1);
       var productEntities = productRepository.findAll(pageable);
       var list = productEntities.get();
       assertThat(list)
               .isNotNull()        
               .hasSize(1)
               .contains(pe);
   }

   @Test
   void findAllProductsWithOffset_Exception() {
       assertThrows(IllegalArgumentException.class, ()-> new OffsetBasedPageRequest(1, 0));
   }
}
