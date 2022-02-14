package com.metao.product.retails.persistence;

import com.metao.product.retails.BaseTest;
import com.metao.product.retails.domain.ProductEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class ProductRepositoryTest extends BaseTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    void findProductEntityById() {
        productRepository.save(productEntity);
        Optional<ProductEntity> entity = productRepository.findProductEntityById(PRODUCT_ID);
        Assertions.assertTrue(entity.isPresent());
        ProductEntity expectedProductEntity = entity.get();
        Assertions.assertEquals(expectedProductEntity, productEntity);
    }

    @Test
    void findProductEntityById_NotFound() {
        Optional<ProductEntity> entity = productRepository.findProductEntityById(PRODUCT_ID);
        Assertions.assertTrue(entity.isEmpty());
    }

    @Test
    void findAllProductsWithOffset() {
        productRepository.save(productEntity);
        Pageable pageable = new OffsetBasedPageRequest(1, 0);
        List<ProductEntity> productEntities = productRepository.findAllProductsWithOffset(pageable);
        assertThat(productEntities)
                .isNotNull()
                .hasSize(1)
                .contains(productEntity);
    }

    @Test
    void findAllProductsWithCategoryAndOffset() {
        productRepository.save(productEntity);
        Pageable pageable = new OffsetBasedPageRequest(1, 0);
        List<ProductEntity> productEntities = productRepository.findAllProductsWithCategoryAndOffset("book", pageable);        assertThat(productEntities)
                .isNotNull()
                .hasSize(1)
                .contains(productEntity);
    }

    @Test
    void findAllProductsWithOffset_Exception() {
        catchThrowableOfType(()-> new OffsetBasedPageRequest(1, -1), IllegalArgumentException.class);

    }
}