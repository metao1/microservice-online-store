package com.metao.book.product.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import com.metao.book.product.util.ProductEntityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@TestPropertySource(properties = { "kafka.enabled=false" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should not find product entity by id when it does not exists")
    void findProductByIdNotFound() {
        // WHEN
        Optional<ProductEntity> entity = productRepository.findByAsin("PRODUCT_ID");

        // THEN
        assertTrue(entity.isEmpty());
    }

    @Test
    @DisplayName("Should find product by id when it already exists")
    void findProductById() {
        // GIVEN
        var product = ProductEntityUtils.createProductEntity("NEW_ASIN", "NEW_CATEGORY");
        productRepository.save(product);

        // WHEN
        var result = productRepository.findByAsin(product.getAsin());

        // THEN
        assertThat(result).isPresent().isEqualTo(Optional.of(product));
    }

    @Test
    @DisplayName("Should find product by asin")
    void findProductByAsin() {
        // GIVEN
        productRepository.save(ProductEntityUtils.createProductEntity("NEW_ASIN", "NEW_CATEGORY"));
        // WHEN
        var product = productRepository.findByAsin("NEW_ASIN").orElseThrow();

        // THEN
        assertThat(product)
                .isNotNull()
                .isEqualTo(ProductEntityUtils.createProductEntity("NEW_ASIN", "NEW_CATEGORY"));
    }

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @DisplayName("Should find all products with offset when two items requested is ok")
    void findAllProductsWithOffsetWhenTwoItemsRequestedIsOk() {
        var pes = ProductEntityUtils.createMultipleProductEntity(2);
        for (ProductEntity pe : pes) {
            Set<ProductCategoryEntity> managedCategories = pe.getCategories().stream()
                    .map(c -> entityManager.unwrap(Session.class)
                            .byNaturalId(ProductCategoryEntity.class)
                            .using("category", c.getCategory())
                            .getReference())
                    .collect(Collectors.toSet());
            pe.setCategories(managedCategories);
            entityManager.persist(pe);
        }

        Pageable pageable = new OffsetBasedPageRequest(0, 2);
        var products = productRepository.findAll(pageable);
        var list = products.get();
        assertThat(list).isNotNull().hasSize(2).satisfies(p -> {
            p.forEach(product -> assertThat(product.getAsin()).isNotNull());
            p.forEach(product -> assertThat(product.getCategories()).hasSize(1).isNotNull());
        });
    }

    @Test
    @DisplayName("Should find product's categories")
    void findProductCategories() {
        // GIVEN
        productRepository.save(ProductEntityUtils.createProductEntity("NEW_ASIN2", "NEW_CATEGORY"));
        // WHEN
        var product = productRepository.findByAsin("NEW_ASIN2").orElseThrow();

        // THEN
        assertThat(product)
                .extracting(ProductEntity::getCategories)
                .satisfies(
                        categories -> assertThat(categories).hasSize(1).element(0)
                                .extracting(ProductCategoryEntity::getCategory)
                                .isEqualTo("NEW_CATEGORY"));
    }
}
