package com.metao.book.product.domain.category;

import com.metao.book.product.domain.model.entity.ProductCategory;
import java.util.Set;

public interface ProductCategoriesInterface {

    Set<ProductCategory> getProductCategories(String productId);
}
