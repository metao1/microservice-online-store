package com.metao.product.application.service;

import java.util.List;
import java.util.Optional;

import com.metao.product.application.exception.ProductNotFoundException;
import com.metao.product.domain.*;
import com.metao.product.infrustructure.repository.model.OffsetBasedPageRequest;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
//@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class ProductService implements ProductServiceInterface {

    private final ProductRepository productRepository;

    @Override
    public Optional<ProductEntity> getProductById(ProductId productId) throws ProductNotFoundException {
        return productRepository.findById(productId);
    }

    @Override
    public Optional<List<ProductEntity>> getAllProductsPageable(int limit, int offset) {
        var pageable = new OffsetBasedPageRequest(offset, limit);
        return Optional.ofNullable(productRepository.findAll(pageable))
                .map(Page::toList);
    }

    @Override
    public void saveProduct(ProductEntity pe) {
        this.productRepository.save(pe);
    }
}
