package com.metao.book.product.domain;

import java.util.Optional;
import java.util.Set;

public interface ProductCategoriesInterface {

    Optional<Set<ProductCategoryEntity>> getProductCategories(ProductId productId);
}
