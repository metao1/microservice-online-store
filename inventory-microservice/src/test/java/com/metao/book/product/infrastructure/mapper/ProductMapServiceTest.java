package com.metao.book.product.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.application.config.ProductMapService;
import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductEvent;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.domain.image.Image;
import com.metao.book.product.util.ProductTestUtils;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ProductMapServiceTest {

    private static final ProductMapService productMapper = new ProductMapService();
    private static final Currency DLR = Currency.getInstance(Locale.US);

    @Test
    void givenProductDto_whenConvertToProductEntity_isOk() {
        var productDto = ProductTestUtils.createProductDTO();
        var productEntity = productMapper.toEntity(productDto);
        assertThat(productEntity)
            .extracting(
                ProductEntity::getTitle,
                ProductEntity::getDescription,
                ProductEntity::getPriceValue,
                ProductEntity::getPriceCurrency,
                ProductEntity::getImage)
            .containsExactly(
                "title",
                "description",
                BigDecimal.valueOf(12),
                DLR,
                new Image("https://example.com/image.jpg"));

        assertThat(productEntity.getCategories())
            .matches(categoryEntities -> categoryEntities
                .contains(new ProductCategoryEntity(new Category("book"))));
    }

    @Test
    void givenProductEntity_whenConvertToProductDTO_isOk() {
        var pe = ProductTestUtils.createProductEntity();
        var productMapper = new ProductMapService();
        var productDto = productMapper.toEvent(pe);
        var categories = pe.getCategories();

        productDto.categories()
            .stream()
            .map(CategoryDTO::getCategory)
            .forEach(category -> assertTrue(categories.contains(new ProductCategoryEntity(new Category(category)))));

        assertThat(productDto)
            .extracting(
                ProductEvent::title,
                ProductEvent::description,
                ProductEvent::price,
                ProductEvent::imageUrl)
            .containsExactly(
                pe.getTitle(),
                pe.getDescription(),
                pe.getPriceValue(),
                pe.getImage().url());
    }

    @Test
    public void givenProductEntities_whenConvertToProductDTOs_isOk() {
        var pe = ProductTestUtils.createProductEntity();
        var allPr = List.of(pe);
        var allPrDtos = productMapper.toDTOList(allPr);
        assertEquals(allPrDtos.size(), allPr.size());
        allPrDtos.forEach(dto -> assertThat(dto)
            .extracting(
                ProductEvent::title,
                ProductEvent::description,
                ProductEvent::price,
                ProductEvent::imageUrl)
            .containsExactly(
                pe.getTitle(),
                pe.getDescription(),
                pe.getPriceValue(),
                pe.getImage().url()));
    }

}
