package com.metao.product.retails.service;

import com.metao.product.retails.BaseTest;
import com.metao.product.retails.mapper.ProductCategoriesMapper;
import com.metao.product.retails.model.ProductCategoriesDTO;
import com.metao.product.retails.persistence.CategoriesRepository;
import com.metao.product.retails.service.impl.ProductCategoriesServiceImplementation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoriesServiceTest extends BaseTest {

    @InjectMocks
    ProductCategoriesServiceImplementation productCategoriesService;

    @Mock
    CategoriesRepository categoriesRepository;

    @Mock
    ProductCategoriesMapper productCategoriesMapper;

    @Test
    void getProductCategories() {
        String productCategoryId = UUID.randomUUID().toString();
        when(productCategoriesMapper.toDto(productCategoryEntity))
                .thenReturn(ProductCategoriesDTO.builder()
                        .categories("book")
                        .id(productCategoryId)
                        .build());
        when(categoriesRepository.findProductEntities())
                .thenReturn(new ArrayList<>(productCategoryEntities));
        List<ProductCategoriesDTO> categories = productCategoriesService
                .getProductCategories();

        Assertions.assertThat(categories)
                .isNotNull()
                .hasSize(1);
    }
}