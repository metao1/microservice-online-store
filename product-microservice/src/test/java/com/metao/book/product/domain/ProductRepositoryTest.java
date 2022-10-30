package com.metao.book.product.domain;

import com.metao.book.product.util.BasePostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest extends BasePostgresIntegrationTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    void findByProductCategory() {
        productRepository.findProductCategoriesByProductId(new ProductId("id"));
    }
}