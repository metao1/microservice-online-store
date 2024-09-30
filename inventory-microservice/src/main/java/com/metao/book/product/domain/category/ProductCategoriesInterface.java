package com.metao.book.product.domain.category;

import com.metao.book.product.domain.ProductId;

import java.util.Set;

public interface ProductCategoriesInterface {

    Set<ProductCategoryEntity> getProductCategories(ProductId productId);
}
