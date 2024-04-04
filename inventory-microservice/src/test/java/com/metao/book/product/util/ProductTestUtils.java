package com.metao.book.product.util;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.domain.image.Image;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.test.StreamBuilderTestUtils.StreamBuilder;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProductTestUtils {

    private static final Currency DLR = Currency.getInstance(Locale.US);
    public static ProductEntity createProductEntity() {
        var description = "description";
        var title = "title";
        var id = "id";
        return createProductEntity(id, title, description);
    }

    public static ProductEntity createProductEntity(String id, String title, String description) {
        var url = "https://example.com/image.jpg";
        var price = BigDecimal.valueOf(12);
        var volume = BigDecimal.valueOf(100d);
        var pe = new ProductEntity(id, title, description, volume, new Money(DLR, price), new Image(url));
        pe.addCategory(createProductCategoryEntity());
        return pe;
    }

    public static ProductCategoryEntity createProductCategoryEntity() {
        return new ProductCategoryEntity(new Category("book"));
    }

    public static List<ProductEntity> createMultipleProductEntity(int size) {
        final var description = "description";
        var title = "title";
        return StreamBuilder.of(ProductEntity.class, 0, size,
                a -> createProductEntity(a.toString(), title + a, description))
            .toList();
    }

    public static ProductEvent createProductDTO() {
        return ProductEvent
            .builder()
            .price(BigDecimal.valueOf(12))
            .title("title")
            .asin("1234567899")
            .currency(DLR)
            .volume(BigDecimal.ONE)
            .description("description")
            .imageUrl("https://example.com/image.jpg")
            .categories(Set.of(CategoryDTO.of("book")))
            .build();
    }
}
