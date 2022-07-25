package com.metao.book.retails.util;

import com.metao.book.retails.application.dto.CategoryDTO;
import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.ProductCategoryEntity;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.category.Category;
import com.metao.book.retails.domain.image.Image;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class ProductTestUtils {

    public static ProductEntity createProductEntity() {
        var description = "description";
        var title = "title";
        var id = "id";
        return createProductEntity(id, title, description);
    }

    public static ProductEntity createProductEntity(String id, String title, String description) {
        var url = "https://example.com/image.jpg";
        var price = 12d;
        var currency = Currency.DLR;
        var category = new ProductCategoryEntity(new Category("book"));
        var pe = new ProductEntity(id, title, description, new Money(currency, price), new Image(url));
        pe.addCategory(category);
        return pe;
    }

    public static List<ProductEntity> creteMultipleProductEntity(int size) {
        final var description = "description";
        var title = "title";
        return IntStream
                .range(0, size)
                .boxed()
                .map(a -> createProductEntity(a.toString(), title + a, description))
                .toList();

    }

    public static ProductDTO createProductDTO() {
        return ProductDTO
                .builder()
                .asin("1234567899")
                .title("title")
                .description("description")
                .price(12d)
                .currency(Currency.DLR)
                .categories(Set.of(CategoryDTO.of("book")))
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

}
