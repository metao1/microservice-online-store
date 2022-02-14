package com.metao.product.retails;

import com.metao.product.retails.domain.ProductCategoryEntity;
import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.model.ProductDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BaseTest {

    protected static final String USER_ID = UUID.randomUUID().toString();
    protected static final String PRODUCT_ID = UUID.randomUUID().toString();
    private static final String productCategoryId = UUID.randomUUID().toString();
    protected ProductCategoryEntity productCategoryEntity = ProductCategoryEntity.builder()
            .id(productCategoryId)
            .categories("book")
            .build();
    protected Set<ProductCategoryEntity> productCategoryEntities =
            Collections.singleton(productCategoryEntity);

    Set<String> productCategoriesDTOs = Collections.singleton("book");

    protected final ProductEntity productEntity = ProductEntity.builder()
            .title("brand")
            .categories(productCategoryEntities)
            .description("clothes")
            .id(PRODUCT_ID)
            .createdAt(NOW())
            .modifiedAt(NOW())
            .createdBy(USER_ID)
            .modifiedBy(USER_ID)
            .imageUrl("image-url")
            .price(1200d)
            .build();

    protected ProductDTO productDTO = ProductDTO.builder()
            .asin(PRODUCT_ID)
            .title("brand")
            .description("clothes")
            .price(1200d)
            .categories(productCategoriesDTOs)
            .build();

    protected LocalDate NOW() {
        return LocalDate.now();
    }
}
