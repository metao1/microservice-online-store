package com.metao.book.product.util;

import static com.metao.book.product.infrastructure.util.ProductConstant.ASIN;

import com.google.protobuf.Timestamp;
import com.metao.book.product.ProductCreatedEvent;
import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory;
import com.metao.book.product.infrastructure.util.ProductConstant;
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

    public static Product createProductEntity() {
        var description = "description";
        var title = "title";
        return createProductEntity(ASIN, title, description, ProductConstant.CATEGORY);
    }

    public static Product createProductEntity(String asin, String category) {
        var description = "description";
        var title = "title";
        return createProductEntity(asin, title, description, category);
    }

    public static Product createProductEntity(String asin, String title, String description, String category) {
        var url = "https://example.com/image.jpg";
        var price = new BigDecimal("12.00");
        var volume = new BigDecimal("100.00");
        var pe = new Product(asin, title, description, volume, new Money(EUR, price), url);
        pe.addCategory(new ProductCategory(category));
        return pe;
    }

    public static List<Product> createMultipleProductEntity(int size) {
        final var description = "description";
        var title = "title";

        return Stream.iterate(0, a -> a + 1)
            .limit(size)
            .map(a -> createProductEntity(a.toString() + a, title + a, description + a, ProductConstant.CATEGORY))
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
            .addAllCategories(List.of(Category.newBuilder().setName(ProductConstant.CATEGORY).build())).build();

    }
}
