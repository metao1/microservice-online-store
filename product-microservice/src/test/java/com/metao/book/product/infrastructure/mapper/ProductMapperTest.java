package com.metao.book.product.infrastructure.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.domain.image.Image;
import com.metao.book.product.util.ProductTestUtils;
import com.metao.book.shared.domain.financial.Currency;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ProductMapperTest {

    private final ProductMapperInterface productMapper = new ProductMapper();

    @Test
    public void givenProductDto_whenConvertToProductEntity_isOk() {
        var productDto = ProductTestUtils.createProductDTO();
        var productEntity = productMapper.toEntity(productDto);
        assertTrue(productEntity.isPresent());
        var pe = productEntity.get();
        assertThat(pe)
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
                Currency.DLR,
                new Image("https://example.com/image.jpg"));

        assertThat(pe.getProductCategory())
                .matches(categoryEntities -> categoryEntities
                        .contains(new ProductCategoryEntity(new Category("book"))));
    }

    @Test
    public void givenProductEntity_whenConvertToProductDTO_isOk() {
        var pe = ProductTestUtils.createProductEntity();
        var productDto = ProductEntity.toDto(pe);
        var categories = pe.getProductCategory();

        productDto.getCategories()
                .stream()
                .map(CategoryDTO::getCategory)
                .forEach(category -> assertTrue(categories.contains(new ProductCategoryEntity(new Category(category)))));

        assertThat(productDto)
                .extracting(
                        ProductDTO::getTitle,
                        ProductDTO::getDescription,
                        ProductDTO::getPrice,
                        ProductDTO::getImageUrl)
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
        var allPrDtos = productMapper.toDtos(allPr);
        assertThat(allPrDtos.size() == allPr.size());
        allPrDtos.forEach(dto -> assertThat(dto)
                .extracting(
                        ProductDTO::getTitle,
                        ProductDTO::getDescription,
                        ProductDTO::getPrice,
                        ProductDTO::getImageUrl)
                .containsExactly(
                        pe.getTitle(),
                        pe.getDescription(),
                        pe.getPriceValue(),
                        pe.getImage().url()));
    }

}
