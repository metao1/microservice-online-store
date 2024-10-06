package com.metao.book.product.domain.service;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.category.ProductCategoryEntity;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    @PersistenceContext
    private final EntityManager entityManager;

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

    public boolean canSaveProduct(ProductEntity productEntity) {
        if (entityManager.getReference(ProductEntity.class, productEntity.getAsin()) != null) {
            return false;
        }
        Set<ProductCategoryEntity> filteredCategories = productEntity.getCategories().stream()
            .filter(category -> entityManager.getReference(ProductCategoryEntity.class, category.getCategory()) != null)
            .collect(Collectors.toSet());
        productEntity.setCategories(filteredCategories);
        return true;
    }

    public void saveProduct(ProductEntity productEntity) {
        if (canSaveProduct(productEntity)) {
            productRepository.save(productEntity);
        }
    }

}
