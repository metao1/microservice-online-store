package com.metao.book.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.util.ProductTestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductCategoriesServiceTest {

    private static final String PRODUCT_ID = "1234567890";

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCategoriesService categoriesService;

    @Test
    @DisplayName("Get product categories found")
    void getProductCategoriesFound() {

        // GIVEN
        var returnedProductEntity = Optional.of(ProductTestUtils.createProductEntity());
        when(productRepository.findById(new ProductId(PRODUCT_ID)))
            .thenReturn(returnedProductEntity);

        // WHEN
        var productCategories = categoriesService.getProductCategories(new ProductId(PRODUCT_ID));

        // THEN
        assertThat(productCategories)
            .isNotNull()
            .isEqualTo(ProductTestUtils.createProductEntity().getCategories());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get product categories not found")
    void testGetProductCategoriesNotFound() {
        assertThatThrownBy(() -> categoriesService.getProductCategories(new ProductId(PRODUCT_ID)));
    }
}