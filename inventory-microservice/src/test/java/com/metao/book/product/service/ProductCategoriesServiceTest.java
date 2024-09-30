package com.metao.book.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.metao.book.product.domain.service.ProductCategoriesService;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.util.ProductTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductCategoriesServiceTest {

    public static final String PRODUCT_ID = "id";
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCategoriesService productCategoriesService;

    @Test
    void getProductCategories() {
        var returnedProductCategories = Optional.of(ProductTestUtils.createProductEntity());
        doReturn(returnedProductCategories)
            .when(productRepository)
            .findById(new ProductId(PRODUCT_ID));

        var categories = productCategoriesService.getProductCategories(new ProductId(PRODUCT_ID));

        Category category = new Category("book");

        assertThat(categories)
            .extracting(ProductCategoryEntity::getCategory)
            .containsExactlyInAnyOrder(category);

    }
}
