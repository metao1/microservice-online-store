package com.metao.product.retails.domain.product;

import java.util.List;

public interface ProductService {

    ProductEntity getProductById(ProductId productId);

    List<ProductEntity> getAllProductsPageable(int limit, int offset);

    void saveProduct(ProductEntity pe);
}
