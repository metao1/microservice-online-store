package com.metao.product.infrustructure.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.metao.ddd.finance.Currency;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.category.Category;
import com.metao.product.domain.image.Image;
import com.metao.product.util.ProductTestUtils;

import org.junit.jupiter.api.Test;

public class ProductMapperTest {

        private ProductMapperInterface productMapper = new ProductMapper();

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
                                                1200d,
                                                Currency.DLR,
                                                new Image("http://example.com/image.jpg"));

                assertThat(pe.getProductCategory())
                                .matches(categoryEntities -> categoryEntities
                                                .contains(new ProductCategoryEntity(new Category("book"))));
        }

        @Test
        public void givenProductEntity_whenConvertToProductDTO_isOk() {
                var pe = ProductTestUtils.createProductEntity();
                var productDto = productMapper.toDto(pe);
                var categories = pe.getProductCategory();
                productDto.getCategories()
                                .stream()
                                .map(CategoryDTO::getCategory)
                                .forEach(category -> categories
                                                .contains(new ProductCategoryEntity(new Category(category))));
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
