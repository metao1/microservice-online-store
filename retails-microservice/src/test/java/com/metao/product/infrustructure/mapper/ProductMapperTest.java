package com.metao.product.infrustructure.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.ProductCategoryEntity;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.category.CategoryEntity;
import com.metao.product.domain.image.Image;

import org.junit.jupiter.api.Test;

public class ProductMapperTest {

        private ProductMapper productMapper = new ProductMapperImpl();

        @Test
        public void givenProductDto_whenConvertToProductEntity_isOk() {

                var url = "http://localhost:8080/image.jpg";
                var asin = "12345";
                var description = "description";
                var title = "title";
                var price = 12d;
                var currency = Currency.DLR;
                var categories = Set.of(CategoryDTO.of("book"));

                var productDto = ProductDTO
                                .builder()
                                .asin(asin)
                                .categories(categories)
                                .currency(currency)
                                .title(title)
                                .price(price)
                                .description(description)
                                .imageUrl(url)
                                .build();

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
                                                new Image("http://localhost:8080/image.jpg"));

                assertThat(pe.getProductCategory())
                                .extracting(ProductCategoryEntity::getCategories)
                                .matches(categoryEntities -> categoryEntities.contains(new CategoryEntity("book")));
        }

        @Test
        public void givenProductEntity_whenConvertToProductDTO_isOk() {
                var url = "http://localhost:8080/image.jpg";
                var description = "description";
                var title = "title";
                var price = 12d;
                var currency = Currency.DLR;
                var category = new CategoryEntity("book");
                var categories = Set.of(CategoryDTO.of("book"));
                var pe = new ProductEntity(title, description, new Money(currency, price), new Image(url));
                pe.addCategory(category);
                var productDto = productMapper.toDto(pe);
                
                assertThat(productDto)
                                .extracting(
                                                ProductDTO::getTitle,
                                                ProductDTO::getDescription,
                                                ProductDTO::getPrice,
                                                ProductDTO::getCategories,
                                                ProductDTO::getImageUrl)
                                .containsExactly(
                                                title,
                                                description,
                                                price * 100,
                                                categories,
                                                url);
        }


        @Test
        public void givenProductEntities_whenConvertToProductDTOs_isOk() {
                var url = "http://localhost:8080/image.jpg";
                var description = "description";
                var title = "title";
                var price = 12d;
                var currency = Currency.DLR;
                var category = new CategoryEntity("book");
                var categories = Set.of(CategoryDTO.of("book"));
                var pe = new ProductEntity(title, description, new Money(currency, price), new Image(url));
                pe.addCategory(category);

                var allPr = List.of(pe);
                var allPrDtos = productMapper.toDtos(allPr);
                assertThat(allPrDtos.size() == allPr.size());
                allPrDtos.forEach(dto-> assertThat(dto)
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
                                                categories,
                                                pe.getImage().url())
                );
        }

}
