package com.metao.book.product.domain.service;

import com.metao.book.product.domain.ProductEntity;
import com.metao.book.product.domain.ProductId;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import com.metao.book.product.infrastructure.repository.model.OffsetBasedPageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Optional<ProductEntity> getProductById(ProductId productId) throws ProductNotFoundException {
        var productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product " + productId + " not found."));
        return Optional.ofNullable(productEntity);
    }

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

    public void saveProduct(ProductEntity productEntity) {
/*
        Set<ProductCategoryEntity> categoriesToUse = new HashSet<>();

        for (ProductCategoryEntity category : productEntity.getCategories()) {
            // Check the local cache first
            ProductCategoryEntity cachedCategory = categoryCache.get(category.id());
            if (cachedCategory != null) {
                categoriesToUse.add(cachedCategory);
            } else {
                // Attempt to find the existing category by ID
                ProductCategoryEntity existingCategory = entityManager.find(ProductCategoryEntity.class, category.id());

                if (existingCategory != null) {
                    // If found, use the existing category and store it in the cache
                    categoriesToUse.add(existingCategory);
                    categoryCache.put(existingCategory.id(), existingCategory);
                } else {
                    // If not found, merge the new category into the persistence context
                    ProductCategoryEntity mergedCategory = entityManager.merge(category);
                    categoriesToUse.add(mergedCategory);
                    categoryCache.put(mergedCategory.id(), mergedCategory); // Cache the merged category
                }
            }
        }

        productEntity.setCategories(categoriesToUse);
        entityManager.persist(productEntity); // Persist the product*/
        productRepository.save(productEntity);
    }

}
