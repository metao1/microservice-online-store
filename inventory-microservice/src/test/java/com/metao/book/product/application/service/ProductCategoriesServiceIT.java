package com.metao.book.product.application.service;

import com.metao.book.product.infrastructure.BasePostgresIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "container"})
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCategoriesServiceIT extends BasePostgresIT {

    @Autowired
    ProductCategoriesService categoriesService;

    @Test
    void getProductCategories() {
        // categoriesService.getProductCategories(new ProductId("id"));
        // categoriesService.getProductCategories(new ProductId("id"));
    }
}