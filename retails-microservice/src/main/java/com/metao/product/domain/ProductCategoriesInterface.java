package com.metao.product.domain;

import java.util.List;

public interface ProductCategoriesInterface {

    List<ProductCategoryEntity> getProductCategories(ProductId productId);
}
