package com.metao.product.retails.service;

import com.metao.product.models.ProductDTO;

import java.util.List;

public interface ProductService {

    ProductDTO getProductById(String objectId);

    List<ProductDTO> getAllProductsPageable(int limit, int offset);

    List<ProductDTO> getAllProductsWithCategory(String category, int limit, int offset);

    void saveProduct(ProductDTO obj);
}
