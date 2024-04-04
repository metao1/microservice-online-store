package com.metao.book.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.metao.book.product.application.dto.CategoryDTO;
import com.metao.book.product.application.service.ProductCategoriesService;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.infrastructure.mapper.ProductCategoryMapper;
import com.metao.book.product.util.ProductTestUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCategoriesServiceTest {

    public static final String PRODUCT_ID = "id";
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCategoriesService productCategoriesService;

    @InjectMocks
    ProductCategoryMapper productCategoriesMapper;

    @Test
    void getProductCategories() {
        var returnedProductCategories = Optional.of(Set.of(ProductTestUtils.createProductEntity()));
        doReturn(returnedProductCategories)
            .when(productRepository)
            .findProductCategoriesByProductId(new ProductId(PRODUCT_ID));
        var categories = productCategoriesService
            .getProductCategories(new ProductId(PRODUCT_ID))
            .map(productCategoriesMapper::convertToDtoSet);

        assertTrue(categories.isPresent());
        assertThat(categories.get())
            .extracting(CategoryDTO::getCategory)
            .isEqualTo(List.of("book"));

    }
}
