package com.metao.book.product.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.util.ProductEntityUtils;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    private static final Currency EUR = Currency.getInstance(Locale.GERMANY);

    @Test
    void givenProductDtoWhenConvertToProductEntityIsOk() {
        var productDto = ProductEntityUtils.productCreatedEvent();
        var productEntity = ProductMapper.fromProductCreatedEvent(productDto);

        assertThat(productEntity).extracting(ProductEntity::getAsin, ProductEntity::getTitle,
                ProductEntity::getDescription, ProductEntity::getPriceValue, ProductEntity::getPriceCurrency,
                ProductEntity::getCategories)
            .containsExactlyInAnyOrder(productDto.getAsin(), productDto.getTitle(), productDto.getDescription(),
                BigDecimal.valueOf(productDto.getPrice()), EUR, Set.of(new ProductCategoryEntity("book")));
    }

    @Test
    void givenProductEntityWhenConvertToProductDTOIsOk() {
        var pe = ProductEntityUtils.createProductEntity();
        var productDto = ProductMapper.toDto(pe);
        var categories = pe.getCategories();

        productDto.categories().stream().map(CategoryDTO::category)
            .forEach(category -> assertTrue(categories.contains(new ProductCategoryEntity(category))));

        assertThat(productDto).extracting(ProductDTO::title, ProductDTO::description, ProductDTO::price,
                ProductDTO::imageUrl)
            .containsExactly(pe.getTitle(), pe.getDescription(), pe.getPriceValue(), pe.getImageUrl());
    }

}
