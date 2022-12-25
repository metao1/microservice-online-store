package com.metao.book.product.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import com.metao.book.product.util.BasePostgresIntegrationTest;
import com.metao.book.product.util.ProductTestUtils;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("container")
@DataJpaTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ProductRepositoryTest extends BasePostgresIntegrationTest {

    @Autowired
    ProductRepository productRepository;

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
        var productEntities = productRepository.findByAsin(pe.getAsin());
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

    @Test
    void findProductById() {
        var pe = ProductTestUtils.createProductEntity();
        productRepository.save(pe);
        var product = productRepository.findById(pe.id());
        assertTrue(product.isPresent());
        assertEquals(product.get(), pe);
    }

    @Test
    void findByProductCategory() {
        var pe = ProductTestUtils.createProductEntity();
        productRepository.save(pe);

        var optionalProductCategories = productRepository.findProductCategoriesByProductId(pe.id());
        assertTrue(optionalProductCategories.isPresent());
        assertThat(optionalProductCategories.get())
                .hasSize(1)
                .haveExactly(1, new Condition<>(m -> pe.id().equals(m.id()), "with this id there is only one product"))
                .element(0)
                .isEqualTo(pe)
                .extracting(ProductEntity::getCategories)
                .satisfies(categories -> {
                    assertThat(categories)
                            .hasSize(1)
                            .element(0)
                            .extracting(ProductCategoryEntity::getCategory)
                            .extracting(Category::category)
                            .isEqualTo("book");
                });

    }
}
