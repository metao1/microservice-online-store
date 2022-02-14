package com.metao.product.retails.domain.category;

import com.metao.product.retails.domain.product.ProductCategoryEntity;
import com.metao.product.retails.domain.product.ProductId;

import java.util.List;

public interface ProductCategoriesService {

    List<ProductCategoryEntity> getProductCategories(ProductId productId);
}
