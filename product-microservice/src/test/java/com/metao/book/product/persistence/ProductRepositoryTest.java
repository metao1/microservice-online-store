package com.metao.book.product.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.domain.ProductCategoryRepository;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import com.metao.book.product.util.BasePostgresIntegrationTest;
import com.metao.book.product.util.ProductTestUtils;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Slf4j
@DataJpaTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductRepositoryTest extends BasePostgresIntegrationTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductCategoryRepository categoryRepository;

    @Test
    void saveProductEntity() {
        var productEntity = ProductTestUtils.createProductEntity();
        productRepository.save(productEntity);
    }

    @Test
    void findProductEntityById_NotFound() {
        Optional<ProductEntity> entity = productRepository.findById(new ProductId("PRODUCT_ID"));
        assertTrue(entity.isEmpty());
    }

    @Test
    void findAllProductsWithOffset_whenOnlyOneItemRequests_isOk() {
        var pe = ProductTestUtils.createProductEntity();
        productRepository.save(pe);
        Pageable pageable = new OffsetBasedPageRequest(0, 1);
        var productEntities = productRepository.findByIsin(pe.getIsin());
        var productEntity = productEntities.get();
        log.error(pe.toString());
        assertThat(productEntity)
            .isNotNull()
            .isEqualTo(pe);
    }

    @Test
    void findAllProductsWithOffset_whenTwoItemsRequests_Ok() {
        var pes = ProductTestUtils.createMultipleProductEntity(2);
        productRepository.saveAll(pes);
        Pageable pageable = new OffsetBasedPageRequest(0, 2);
        var productEntities = productRepository.findAll(pageable);
        var list = productEntities.get();
        assertThat(list)
            .isNotNull()
            .hasSize(2);
    }

    @Test
    void findAllProductsWithOffset_Exception() {
        assertThrows(IllegalArgumentException.class, () -> new OffsetBasedPageRequest(1, 0));
    }
}
