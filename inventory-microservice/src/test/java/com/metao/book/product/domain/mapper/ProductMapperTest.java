package com.metao.book.product.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.Timestamp;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.event.ProductCreatedEvent;
import com.metao.book.shared.CategoryOuterClass.Category;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductMapperTest {

    @Test
    void toDtoAssertThrowsWhenProductEntityIsNull() {
        // THEN
        assertThatThrownBy(() -> ProductMapper.toDto(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void toProductCreatedEventAssertThrowsWhenProductDTOIsNull() {
        // THEN
        assertThatThrownBy(() -> ProductMapper.toProductCreatedEvent(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void fromProductCreatedEventAssertThrowsWhenProductCreatedEventIsNull() {
        // THEN
        assertThatThrownBy(() -> ProductMapper.fromProductCreatedEvent(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void toDtoToProductEntityReturnSuccessfully() {
        // THEN
        var productDto = ProductDTO.builder()
            .asin("ASIN")
            .title("title")
            .description("description")
            .price(BigDecimal.ONE)
            .currency(Currency.getInstance("EUR"))
            .volume(BigDecimal.ONE)
            .imageUrl("https://example.com/image.jpg")
            .boughtTogether(List.of("ASIN1", "ASIN2"))
            .categories(Set.of(new CategoryDTO("category")))
            .build();

        // WHEN
        var productEntity = ProductMapper.fromDto(productDto);

        // THEN
        assertThat(productEntity).satisfies(pe -> {
            assertThat(pe.getAsin()).isEqualTo(productDto.asin());
            assertThat(pe.getTitle()).isEqualTo(productDto.title());
            assertThat(pe.getDescription()).isEqualTo(productDto.description());
            assertThat(pe.getPriceValue()).isEqualTo(productDto.price());
            assertThat(pe.getPriceCurrency()).isEqualTo(productDto.currency());
            assertThat(pe.getVolume()).isEqualTo(productDto.volume());
            assertThat(pe.getImageUrl()).isEqualTo(productDto.imageUrl());
            assertThat(pe.getCategories())
                .extracting(ProductCategoryEntity::getCategory)
                .containsExactlyInAnyOrderElementsOf(
                    productDto.categories().stream().map(CategoryDTO::category).toList()
                );
        });
    }

    @Test
    void toProductEntityFromProductCreatedEventSuccessfully() {
        // GIVEN
        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.newBuilder()
            .setAsin("ASIN")
            .setTitle("title")
            .setDescription("description")
            .setPrice(1.0)
            .setCurrency("EUR")
            .setVolume(1.0)
            .setImageUrl("https://example.com/image.jpg")
            .addAllCategories(List.of(Category.newBuilder().setName("category").build()))
            .addAllBoughtTogether(List.of("ASIN1", "ASIN2"))
            .setCreateTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .build();

        // WHEN
        var productEntity = ProductMapper.fromProductCreatedEvent(productCreatedEvent);

        // THEN
        assertThat(productEntity).satisfies(pe -> {
            assertThat(pe.getAsin()).isEqualTo(productCreatedEvent.getAsin());
            assertThat(pe.getTitle()).isEqualTo(productCreatedEvent.getTitle());
            assertThat(pe.getDescription()).isEqualTo(productCreatedEvent.getDescription());
            assertThat(pe.getPriceValue().doubleValue()).isEqualTo(productCreatedEvent.getPrice());
            assertThat(pe.getPriceCurrency()).isEqualTo(Currency.getInstance(productCreatedEvent.getCurrency()));
            assertThat(pe.getVolume()).isEqualByComparingTo(BigDecimal.valueOf(productCreatedEvent.getVolume()));
            assertThat(pe.getImageUrl()).isEqualTo(productCreatedEvent.getImageUrl());
            assertThat(pe.getCategories())
                .extracting(ProductCategoryEntity::getCategory)
                .containsExactlyInAnyOrderElementsOf(
                    productCreatedEvent.getCategoriesList().stream().map(Category::getName).toList()
                );
        });
    }

    @Test
    void fromProductCreatedEvent() {
    }
}