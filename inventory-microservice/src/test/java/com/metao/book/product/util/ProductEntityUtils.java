package com.metao.book.product.util;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.CategoryOuterClass.Category;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductEntityUtils {

    private static final Currency EUR = Currency.getInstance("EUR");
    public static final String ASIN = "ABCDEF1234";

    public static ProductEntity createProductEntity() {
        var description = "description";
        var title = "title";
        var category = "category";
        return createProductEntity(ASIN, title, description, category);
    }

    public static ProductEntity createProductEntity(String asin, String category) {
        var description = "description";
        var title = "title";
        return createProductEntity(asin, title, description, category);
    }

    public static ProductEntity createProductEntity(String asin, String title, String description, String category) {
        var url = "https://example.com/image.jpg";
        var price = new BigDecimal("12.00");
        var volume = new BigDecimal("100.00");
        var pe = new ProductEntity(asin, title, description, volume, new Money(EUR, price), url);
        pe.addCategory(new ProductCategoryEntity(category));
        return pe;
    }

    public static List<ProductEntity> createMultipleProductEntity(int size) {
        final var description = "description";
        var title = "title";

        return Stream.iterate(0, a -> a + 1)
            .limit(size)
            .map(a -> createProductEntity(a.toString() + a, title + a, description, "book"))
            .toList();
    }

    public static ProductCreatedEvent productCreatedEvent() {
        return ProductCreatedEvent.newBuilder()
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setAsin("1234567899")
            .setCurrency(EUR.getCurrencyCode())
            .setPrice(100d)
            .setTitle("TITLE")
            .setDescription("DESCRIPTION")
            .setImageUrl("IMAGE_URL")
            .addAllCategories(List.of(Category.newBuilder().setName("book").build())).build();

    }
}
