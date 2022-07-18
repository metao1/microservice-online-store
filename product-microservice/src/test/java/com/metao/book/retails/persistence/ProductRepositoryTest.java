package com.metao.book.retails.persistence;

import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.ProductId;
import com.metao.book.retails.domain.ProductRepository;
import com.metao.book.retails.infrustructure.repository.model.OffsetBasedPageRequest;
import com.metao.book.retails.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
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
    void findAllProductsWithOffset_whenOnlyOneItemRequests_isOk() {
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
    void findAllProductsWithOffset_whenTwoItemsRequests_Ok() {
        var pes = ProductTestUtils.creteMultipleProductEntity(2);
        pes.forEach(productRepository::save);
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
