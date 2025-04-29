package com.metao.book.product.domain;

import static com.metao.book.product.infrastructure.util.ProductConstant.ASIN;
import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.product.domain.category.ProductCategory;
import com.metao.book.product.infrastructure.util.ProductConstant;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void testEquals() {
        var productEntity = new Product(
            ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        var productEntity2 = new Product(
            ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        assertThat(productEntity).isEqualTo(productEntity2);
    }

    @Test
    void testNotEquals() {
        var productEntity = new Product(
            ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        var productEntity2 = new Product(
            ProductConstant.NEW_ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        assertThat(productEntity).isNotEqualTo(productEntity2);
    }

    @Test
    void typeTest() {
        Product product = new Product(
            ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        Object o = new Product(
            ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        assertThat(product).isEqualTo(o);
    }

    @Test
    void productCategoryTest() {
        Product product = new Product(
            ASIN,
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            "https://example.com/image.jpg"
        );

        ProductCategory productCategory = new ProductCategory(ProductConstant.CATEGORY);

        product.addCategory(productCategory);

        assertThat(product.getCategories()).contains(new ProductCategory(ProductConstant.CATEGORY));
    }
}