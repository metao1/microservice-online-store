package com.metao.book.retails.domain;

import java.util.List;
import java.util.Optional;

public interface ProductServiceInterface {

    Optional<ProductEntity> getProductById(String productId);

    Optional<List<ProductEntity>> getAllProductsPageable(int limit, int offset);

    void saveProduct(ProductEntity pe);

}
