package com.metao.product.retails.infrustructure.mapper;

import com.metao.ddd.finance.Currency;
import com.metao.product.retails.application.dto.CategoryDTO;
import com.metao.product.retails.application.dto.ProductDTO;
import com.metao.product.retails.domain.category.CategoryEntity;
import com.metao.product.retails.domain.image.Image;
import com.metao.product.retails.domain.product.ProductCategoryEntity;
import com.metao.product.retails.domain.product.ProductEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ProductMapperTest {

    @Test
    public void givenProductDto_whenConvertToProductEntity_isOk() {
        ProductMapper productMapper = new ProductMapperImpl();
        var productDTO = new ProductDTO();
        productDTO.setCategories(List.of(CategoryDTO
                .builder()
                .category("book")
                .build()));
        productDTO.setAsin(UUID.randomUUID().toString());
        productDTO.setTitle("no_title");
        productDTO.setImageUrl("http://localhost:8080/image.jpg");
        productDTO.setDescription("no_description");
        productDTO.setCurrency(Currency.DLR);
        productDTO.setPrice(12d);
        var productEntity = productMapper.toEntity(productDTO);
        assertTrue(productEntity.isPresent());
        var pe = productEntity.get();
        assertThat(pe)
                .extracting(
                        ProductEntity::getTitle,
                        ProductEntity::getDescription,
                        ProductEntity::getPriceValue,
                        ProductEntity::getPriceCurrency,
                        ProductEntity::getImage
                )
                .containsExactly(
                        "no_title",
                        "no_description",
                        1200d,
                        Currency.DLR,
                        new Image("http://localhost:8080/image.jpg")
                );

        assertThat(pe.getProductCategory())
                .extracting(ProductCategoryEntity::getCategories)
                .matches(categoryEntities -> categoryEntities.contains(new CategoryEntity("book")));
    }

}
