package com.metao.book.retails.application.service;

import java.util.List;
import java.util.Optional;

import com.metao.book.retails.application.exception.ProductNotFoundException;
import com.metao.book.retails.domain.*;
import com.metao.book.retails.infrustructure.repository.model.OffsetBasedPageRequest;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class ProductService implements ProductServiceInterface {

    private final ProductRepository productRepository;

    @Override
    public Optional<ProductEntity> getProductById(ProductId productId) throws ProductNotFoundException {
        return Optional.of(productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product " + productId + " not found.")));
    }

    @Override
    public Optional<List<ProductEntity>> getAllProductsPageable(int limit, int offset) throws ProductNotFoundException {
        var pageable = new OffsetBasedPageRequest(offset, limit);
        var option = Optional.ofNullable(productRepository.findAll(pageable));
        return Optional.of(option.map(Page::toList))
                .orElseThrow(() -> new ProductNotFoundException("product list is empty."));
    }

    @Override
    public void saveProduct(ProductEntity pe) {
        this.productRepository.save(pe);
    }
}