package com.metao.product.retails.service.impl;


import com.metao.product.retails.mapper.ProductCategoriesMapper;
import com.metao.product.retails.model.ProductCategoriesDTO;
import com.metao.product.retails.persistence.CategoriesRepository;
import com.metao.product.retails.service.ProductCategoriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("productCategoriesService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCategoriesServiceImplementation implements ProductCategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final ProductCategoriesMapper productCategoriesMapper;

    @Override
    public List<ProductCategoriesDTO> getProductCategories() {
        return categoriesRepository.findProductEntities()
                .stream()
                .map(productCategoriesMapper::toDto)
                .collect(Collectors.toList());
    }
}
