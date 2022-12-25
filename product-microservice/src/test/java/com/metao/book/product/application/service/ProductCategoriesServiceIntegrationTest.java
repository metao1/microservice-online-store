package com.metao.book.product.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.metao.book.product.util.BasePostgresIntegrationTest;

import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCategoriesServiceIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    ProductCategoriesService categoriesService;

    @Test
    void getProductCategories() {
        // categoriesService.getProductCategories(new ProductId("id"));
    }
}