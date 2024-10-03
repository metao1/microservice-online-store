package com.metao.book.product.domain.service;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    
    public Optional<ProductEntity> getProductByAsin(String asin) throws ProductNotFoundException {
        var productEntity = productRepository.findByAsin(asin)
            .orElseThrow(() -> new ProductNotFoundException("product " + asin + " not found."));
        return Optional.ofNullable(productEntity);
    }

    public Stream<ProductEntity> getAllProductsPageable(int limit, int offset) throws ProductNotFoundException {
        var pageable = new OffsetBasedPageRequest(offset, limit);
        var pagedProducts = productRepository.findAll(pageable);
        if (pagedProducts.isEmpty()) {
            throw new ProductNotFoundException("products not found.");
        }
        return pagedProducts.get();
    }

    public Stream<ProductEntity> getProductsByCategory(int limit, int offset, String category)
        throws ProductNotFoundException {
        var pageable = new OffsetBasedPageRequest(offset, limit);
        var products = productRepository.findAllByCategories(category, pageable);
        if (products.isEmpty()) {
            throw new ProductNotFoundException("products not found.");
        }
        return products.stream();
    }

    public void saveProduct(ProductEntity productEntity) {
        productRepository.save(productEntity);
    }

}
