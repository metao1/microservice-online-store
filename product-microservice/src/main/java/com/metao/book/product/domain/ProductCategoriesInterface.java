package com.metao.book.product.domain;

import java.util.Set;

public interface ProductCategoriesInterface {

    Set<ProductCategoryEntity> getProductCategories(String productId);
}
