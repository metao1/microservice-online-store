package com.metao.book.retails.infrustructure.mapper;

import com.metao.book.retails.application.dto.CategoryDTO;
import com.metao.book.retails.application.dto.ProductDTO;
import com.metao.book.retails.domain.ProductCategoryEntity;
import com.metao.book.retails.domain.ProductEntity;
import com.metao.book.retails.domain.category.Category;
import com.metao.book.retails.domain.image.Image;
import com.metao.book.retails.util.ProductTestUtils;
import com.metao.book.shared.domain.financial.Currency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                                                12d,
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
