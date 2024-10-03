package com.metao.book.product.util;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.event.Category;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.test.StreamBuilderTestUtils.StreamBuilder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductTestUtils {

    private static final Currency EUR = Currency.getInstance("EUR");

    public static ProductEntity createProductEntity() {
        var description = "description";
        var title = "title";
        var asin = "asin";
        var category = "category";
        return createProductEntity(asin, title, description, category);
    }

    public static ProductEntity createProductEntity(String asin, String category) {
        var description = "description";
        var title = "title";
        return createProductEntity(asin, title, description, category);
    }

    public static ProductEntity createProductEntity(String asin, String title, String description, String category) {
        var url = "https://example.com/image.jpg";
        var price = BigDecimal.valueOf(12);
        var volume = BigDecimal.valueOf(100d);
        var pe = new ProductEntity(asin, title, description, volume, new Money(EUR, price), url);
        pe.addCategory(new ProductCategoryEntity(category));
        return pe;
    }

    public static List<ProductEntity> createMultipleProductEntity(int size) {
        final var description = "description";
        var title = "title";
        return StreamBuilder.of(ProductEntity.class, 0, size,
            a -> createProductEntity(a.toString(), title + a, description, a.toString())).toList();
    }

    public static ProductDTO productDTO() {
        return ProductDTO.builder().price(BigDecimal.valueOf(12d)).title("title").asin("1234567899").currency(EUR)
            .volume(BigDecimal.ONE).description("description").imageUrl("https://example.com/image.jpg")
            .categories(Set.of(new CategoryDTO("book"))).build();
    }

    public static ProductCreatedEvent productCreatedEvent() {
        return ProductCreatedEvent.newBuilder()
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setAsin("1234567899").setCurrency(EUR.getCurrencyCode()).setPrice(100d).setTitle("TITLE")
            .setDescription("DESCRIPTION").setImageUrl("IMAGE_URL")
            .addAllCategories(List.of(Category.newBuilder().setName("book").build())).build();

    }
}
