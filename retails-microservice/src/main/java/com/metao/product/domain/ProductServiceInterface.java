package com.metao.product.domain;

import java.util.List;

public interface ProductServiceInterface {

    ProductEntity getProductById(ProductId productId);

    List<ProductEntity> getAllProductsPageable(int limit, int offset);

    void saveProduct(ProductEntity pe);
}
