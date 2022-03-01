package com.metao.product.infrustructure.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.metao.ddd.finance.Currency;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.category.CategoryEntity;
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
                                .extracting(ProductCategoryEntity::getCategories)
                                .matches(categoryEntities -> categoryEntities.contains(new CategoryEntity("book")));
        }

        @Test
        public void givenProductEntity_whenConvertToProductDTO_isOk() {
                var pe = ProductTestUtils.createProductEntity();
                var productDto = productMapper.toDto(pe);

                assertThat(productDto)
                                .extracting(
                                                ProductDTO::getTitle,
                                                ProductDTO::getDescription,
                                                ProductDTO::getPrice,
                                                ProductDTO::getCategories,
                                                ProductDTO::getImageUrl)
                                .containsExactly(
                                                pe.getTitle(),
                                                pe.getDescription(),
                                                pe.getPriceValue() * 100,
                                                pe.getProductCategory(),
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
                                                ProductDTO::getCategories,
                                                ProductDTO::getImageUrl)
                                .containsExactly(
                                                pe.getTitle(),
                                                pe.getDescription(),
                                                pe.getPriceValue(),
                                                pe.getProductCategory(),
                                                pe.getImage().url()));
        }

}
