package com.metao.book.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.service.ProductCategoriesService;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.util.ProductEntityUtils;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        var returnedProductEntity = Optional.of(ProductEntityUtils.createProductEntity());
        when(productRepository.findByAsin(PRODUCT_ID))
                .thenReturn(returnedProductEntity);

        // WHEN
        var productCategories = categoriesService.getProductCategories(PRODUCT_ID);

        // THEN
        assertThat(productCategories)
                .isNotNull()
                .isEqualTo(ProductEntityUtils.createProductEntity().getCategories());
    }

    @Test
    @SneakyThrows
    @DisplayName("Get product categories not found")
    void testGetProductCategoriesNotFound() {
        assertThatThrownBy(() -> categoriesService.getProductCategories(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(PRODUCT_ID);
    }
}