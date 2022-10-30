package com.metao.book.product.application.service;

import com.metao.book.product.util.BasePostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductCategoriesServiceIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    ProductCategoriesService categoriesService;

    @Test
    void getProductCategories() {
        categoriesService.getProductCategories("id");
    }
}