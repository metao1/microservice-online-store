package com.metao.product.application.service;

import com.metao.product.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("productCategoriesService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCategoriesServiceImpl implements ProductCategoriesService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductCategoryEntity> getProductCategories(ProductId productId) {
        return productRepository
                .findProductEntityById(productId)
                .stream()
                .map(ProductEntity::getProductCategory)
                .toList();
    }
}
