package com.metao.product.domain;

import java.util.List;

public interface ProductCategoriesService {

    List<ProductCategoryEntity> getProductCategories(ProductId productId);
}
