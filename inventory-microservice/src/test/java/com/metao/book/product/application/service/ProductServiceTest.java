package com.metao.book.product.application.service;

import static com.metao.book.product.infrastructure.util.ProductConstant.ASIN;
import static com.metao.book.product.infrastructure.util.ProductConstant.CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.metao.book.product.domain.category.ProductCategory;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.service.ProductService;
import com.metao.book.product.util.ProductEntityUtils;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@Profile("test")
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource(properties = "kafka.enabled=true")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ProductServiceTest {

    @Autowired
    ProductService productService;

    @BeforeEach
    void setUp() {
        productService.saveProduct(ProductEntityUtils.createProductEntity());
    }

    @Test
    void getProductByIdNotFound() {
        assertThrows(ProductNotFoundException.class, () -> productService.getProductByAsin("1"));
    }

    @Test
    void getProductByIdIsFound() {
        var pe = productService.getProductByAsin(ASIN);
        assertThat(pe)
                .isNotNull()
                .isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getAsin()).isEqualTo(ASIN);
                    assertThat(p.getTitle()).isEqualTo("title");
                    assertThat(p.getDescription()).isEqualTo("description");
                    assertThat(p.getPriceValue()).isEqualTo(new BigDecimal("12.00"));
                    assertThat(p.getPriceCurrency()).isEqualTo(Currency.getInstance("EUR"));
                    assertThat(p.getVolume()).isEqualTo(new BigDecimal("100.00"));
                    assertThat(p.getImageUrl()).isEqualTo("https://example.com/image.jpg");
                    assertThat(p.getCategories())
                            .hasSize(1)
                            .first()
                            .extracting(ProductCategory::getCategory)
                            .isEqualTo(CATEGORY);
                });
    }

    @Test
    void testSaveAnotherProductIsSuccessful() {
        // GIVEN
        var pe = ProductEntityUtils.createProductEntity(ASIN, CATEGORY);
        productService.saveProduct(pe);

        // WHEN
        var newProduct = productService.getProductByAsin(pe.getAsin());

        // THEN
        assertThat(newProduct)
                .isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getAsin()).isEqualTo(ASIN);
                    assertThat(p.getTitle()).isEqualTo("title");
                    assertThat(p.getDescription()).isEqualTo("description");
                    assertThat(p.getPriceValue()).isEqualTo(new BigDecimal("12.00"));
                    assertThat(p.getPriceCurrency()).isEqualTo(Currency.getInstance("EUR"));
                    assertThat(p.getVolume()).isEqualTo(new BigDecimal("100.00"));
                    assertThat(p.getImageUrl()).isEqualTo("https://example.com/image.jpg");
                    assertThat(p.getCategories())
                            .hasSize(1)
                            .first()
                            .extracting(ProductCategory::getCategory)
                            .isEqualTo(CATEGORY);
                });
    }
}
