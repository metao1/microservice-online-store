package com.metao.book.product.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductMapper;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.util.ProductTestUtils;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    private static final Currency EUR = Currency.getInstance(Locale.GERMANY);

    @InjectMocks
    ProductEventMapper productEventMapper;

    @Test
    void givenProductDtoWhenConvertToProductEntityIsOk() {
        var productDto = ProductTestUtils.productCreatedEvent();
        var productEntity = productEventMapper.toEntity(productDto);

        assertThat(productEntity)
            .extracting(
                ProductEntity::getAsin,
                ProductEntity::getTitle,
                ProductEntity::getDescription,
                ProductEntity::getPriceValue,
                ProductEntity::getPriceCurrency,
                ProductEntity::getCategories)
            .containsExactlyInAnyOrder(
                productDto.getAsin(),
                productDto.getTitle(),
                productDto.getDescription(),
                BigDecimal.valueOf(productDto.getPrice()),
                EUR,
                Set.of(new ProductCategoryEntity(new Category("book")))
            );
    }

    @Test
    void givenProductEntityWhenConvertToProductDTOIsOk() {
        var pe = ProductTestUtils.createProductEntity();
        var productMapper = new ProductMapper();
        var productDto = productMapper.toDto(pe);
        var categories = pe.getCategories();

        productDto.categories()
            .stream()
            .map(CategoryDTO::getCategory)
            .forEach(category -> assertTrue(categories.contains(new ProductCategoryEntity(new Category(category)))));

        assertThat(productDto)
            .extracting(
                ProductDTO::title,
                ProductDTO::description,
                ProductDTO::price,
                ProductDTO::imageUrl)
            .containsExactly(
                pe.getTitle(),
                pe.getDescription(),
                pe.getPriceValue(),
                pe.getImage().url());
    }

}
