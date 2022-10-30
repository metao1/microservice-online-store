package com.metao.book.product.service;

import static org.mockito.Mockito.doReturn;

import com.metao.book.product.application.service.ProductCategoriesService;
import com.metao.book.product.domain.ProductCategoryEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.ProductRepository;
import com.metao.book.product.domain.category.Category;
import com.metao.book.product.infrastructure.mapper.ProductCategoryMapper;
import com.metao.book.product.util.ProductTestUtils;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCategoriesServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCategoriesService productCategoriesService;

    @Mock
    ProductCategoryMapper productCategoriesMapper;

    @Test
    void getProductCategories() {
        var productCategory = Set.of(ProductTestUtils.createProductCategoryEntity());
        var returnedProductCategories = Optional.of(Set.of(new ProductCategoryEntity(new Category("book"))));
        doReturn(returnedProductCategories)
            .when(productRepository)
            .findProductEntitiesByProductId(new ProductId("id"));
        var categories = productCategoriesService
            .getProductCategories("id")
            .map(productCategoriesMapper::convertToDtoSet);

        Assertions.assertThat(categories)
            .isNotNull();
    }
}
