package com.metao.book.retails.domain;

import java.util.Set;

public interface ProductCategoriesInterface {

    Set<ProductCategoryEntity> getProductCategories(String productId);
}
