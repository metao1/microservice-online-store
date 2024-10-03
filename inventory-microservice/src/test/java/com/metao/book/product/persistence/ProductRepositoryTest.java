package com.metao.book.product.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import com.metao.book.product.util.ProductTestUtils;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

@Transactional
@TestPropertySource(
    properties = {
        "kafka.isEnabled=false"
    }
)
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    private static final String ASIN = "asin";

    @Autowired
    ProductRepository productRepository;

    @BeforeAll
    @DisplayName("Init database with a product")
    void init() {
        var product = ProductTestUtils.createProductEntity();
        productRepository.save(product);
    }

    @Test
    @DisplayName("Should not find product entity by id when it does not exists")
    void findProductByIdNotFound() {
        //WHEN
        Optional<ProductEntity> entity = productRepository.findByAsin("PRODUCT_ID");

        //THEN
        assertTrue(entity.isEmpty());
    }

    @Test
    @DisplayName("Should find product by id when it already exists")
    void findProductById() {
        //GIVEN
        var product = ProductTestUtils.createProductEntity("NEW_ASIN", "NEW_CATEGORY");
        productRepository.save(product);

        //WHEN
        var result = productRepository.findByAsin(product.getAsin());

        //THEN
        assertThat(result)
            .isPresent()
            .isEqualTo(Optional.of(product));
    }

    @Test
    @DisplayName("Should find product by asin")
    void findProductByAsin() {
        //WHEN GIVEN
        var product = productRepository.findByAsin(ASIN).orElseThrow();

        //THEN
        assertThat(product)
            .isNotNull()
            .isEqualTo(ProductTestUtils.createProductEntity());
    }

    @Test
    @DisplayName("Should find all products with offset when two items requested is ok")
    void findAllProductsWithOffsetWhenTwoItemsRequestedIsOk() {
        var pes = ProductTestUtils.createMultipleProductEntity(2);
        productRepository.saveAll(pes);
        Pageable pageable = new OffsetBasedPageRequest(0, 2);
        var products = productRepository.findAll(pageable);
        var list = products.get();
        assertThat(list)
            .isNotNull()
            .hasSize(2);
    }

    @Test
    @DisplayName("Should find product's categories")
    void findProductCategories() {
        //WHEN GIVEN
        var product = productRepository.findByAsin(ASIN).orElseThrow();

        //THEN
        assertThat(product)
            .extracting(ProductEntity::getCategories)
            .satisfies(categories ->
                assertThat(categories)
                    .hasSize(1)
                    .element(0)
                    .extracting(ProductCategoryEntity::getCategory)
                    .isEqualTo("category")
            );
    }
}
