package com.metao.product.retails.service.impl;

import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.exception.ProductNotFoundException;
import com.metao.product.retails.mapper.ProductMapper;
import com.metao.product.models.ProductDTO;
import com.metao.product.retails.persistence.OffsetBasedPageRequest;
import com.metao.product.retails.persistence.ProductRepository;
import com.metao.product.retails.service.ProductService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("productService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDTO findProductById(@NonNull String productId) {
        Optional<ProductEntity> productEntity = productRepository.findProductEntityById(productId);
        ProductEntity entity = productEntity.orElseThrow(() -> new ProductNotFoundException(productId));
        return productMapper.toDto(entity);
    }

    @Override
    public List<ProductDTO> findAllProductsPageable(int limit, int offset) {
        Pageable pageable = new OffsetBasedPageRequest(limit, offset);
        List<ProductEntity> productEntities = productRepository.findAllProductsWithOffset(pageable);
        return productEntities.stream()
                .filter(Objects::nonNull)
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> findAllProductsWithCategory(String category, int limit, int offset) {
        Pageable pageable = new OffsetBasedPageRequest(limit, offset);
        List<ProductEntity> allProductsWithCategoryAndOffset = this.productRepository.findAllProductsWithCategoryAndOffset(category, pageable);
        return allProductsWithCategoryAndOffset.stream()
                .filter(Objects::nonNull)
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void saveProduct(ProductDTO obj) {
        this.productRepository.saveAndFlush(productMapper.toEntity(obj));
    }
}
