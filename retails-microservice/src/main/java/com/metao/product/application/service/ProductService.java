package com.metao.product.application.service;

import java.util.List;

import com.metao.product.application.exception.ProductNotFoundException;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.ProductId;
import com.metao.product.domain.ProductRepository;
import com.metao.product.domain.ProductServiceInterface;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductServiceInterface {

    private final ProductRepository productRepository;

    @Override
    public ProductEntity getProductById(ProductId productId) throws ProductNotFoundException {
        return productRepository.findProductEntityById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toUUID()));
    }

    @Override
    public List<ProductEntity> getAllProductsPageable(int limit, int offset) {
        return productRepository.findAllWithOffsetOptional(limit, offset)
                    .orElseThrow(() -> new RuntimeException("no product exist"));
    }

    @Override
    public void saveProduct(ProductEntity pe) {
        this.productRepository.save(pe);
    }

    // public List<ProductEntity>
    // getAllProductsWithCategory(ProductCategoriesService categoriesService, String
    // category, int limit, int offset) {
    // Pageable pageable = new OffsetBasedPageRequest(limit, offset);
    // categoriesService.getProductCategories()
    // var result = this.productRepository.findAllProductsWithCategoryAndOffset(,
    // category, pageable);
    // return result.stream()
    // .filter(Objects::nonNull)
    // .map(productMapper::mapToDto)
    // .collect(Collectors.toList());
    // }

}
