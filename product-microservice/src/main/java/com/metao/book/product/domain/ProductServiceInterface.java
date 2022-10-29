package com.metao.book.product.domain;

import java.util.List;
import java.util.Optional;

public interface ProductServiceInterface {

    Optional<ProductEntity> getProductById(ProductId productId);

    Optional<List<ProductEntity>> getAllProductsPageable(int limit, int offset);

    void saveProduct(ProductEntity pe);

}
