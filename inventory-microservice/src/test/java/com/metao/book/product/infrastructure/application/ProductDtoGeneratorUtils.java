package com.metao.book.product.infrastructure.application;

import static com.metao.book.product.infrastructure.util.ProductConstant.SKU;

import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.infrastructure.util.ProductConstant;
import com.metao.book.shared.domain.product.ProductSku;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductDtoGeneratorUtils {

    private static final Currency EUR = Currency.getInstance("EUR");

    public static CreateProductDto buildOneProduct() {
        var description = "description";
        var title = "title";
        return buildOneProduct(SKU, title, description, ProductConstant.CATEGORY);
    }

    public static CreateProductDto buildOneProduct(
        ProductSku sku,
        String title,
        String description,
        CategoryName category
    ) {
        var url = "https://example.com/image.jpg";
        var price = BigDecimal.valueOf(12.00);
        var volume = BigDecimal.valueOf(100.00);
        return new CreateProductDto(
            sku.value(),
            title,
            description,
            url,
            price,
            EUR,
            volume,
            Set.of(category.value())
        );
    }

    public static List<CreateProductDto> buildMultipleProducts(int size) {
        final var description = "description";
        var title = "title";

        return Stream.iterate(1000000000, a -> a + 1)
            .limit(size)
            .map(a -> buildOneProduct(ProductSku.generate(), title + a, description + a, ProductConstant.CATEGORY))
            .toList();
    }
}