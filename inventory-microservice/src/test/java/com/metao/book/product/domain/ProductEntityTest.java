package com.metao.book.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.image.Image;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class ProductEntityTest {

    @Test
    void testEquals() {
        var productEntity = new ProductEntity(
            "ASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        var productEntity2 = new ProductEntity(
            "ASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        assertThat(productEntity).isEqualTo(productEntity2);
    }

    @Test
    void testNotEquals() {
        var productEntity = new ProductEntity(
            "ASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        var productEntity2 = new ProductEntity(
            "otherASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        assertThat(productEntity).isNotEqualTo(productEntity2);
    }

    @Test
    void typeTest() {
        ProductEntity productEntity = new ProductEntity(
            "ASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        Object o = new ProductEntity(
            "ASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        assertThat(productEntity).isEqualTo(o);
    }

    @Test
    void productCategoryTest() {
        ProductEntity productEntity = new ProductEntity(
            "ASIN",
            "title",
            "description",
            BigDecimal.ONE,
            new Money(Currency.getInstance("EUR"), BigDecimal.ONE),
            new Image("https://example.com/image.jpg")
        );

        ProductCategoryEntity productCategoryEntity = new ProductCategoryEntity("book");

        productEntity.addCategory(productCategoryEntity);

        assertThat(productEntity.getCategories()).contains(new ProductCategoryEntity("book"));
    }
}