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
import org.hibernate.Session;
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

    public Optional<ProductEntity> getProductByAsin(String asin) {
        var productEntity = productRepository.findByAsin(asin)
            .orElseThrow(() -> new ProductNotFoundException(asin));
        return Optional.ofNullable(productEntity);
    }

    public Stream<ProductEntity> getAllProductsPageable(int limit, int offset) {
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

    public boolean saveProduct(ProductEntity productEntity) {
        ProductEntity existingProductEntity = findEntityByReference(ProductEntity.class, productEntity.getAsin(),
            "asin");
        if (existingProductEntity != null) {
            return false;
        }
        // product needs to be saved first to have a managed state, when categories are added later
        // the product will be updated the hibernates is able to track the changes and hence saves the categories in
        // the product relationship. Otherwise, if categories are added directly before the product is saved, the
        // categories will be in transient state and will not be tracked by hibernates.
        productRepository.save(productEntity);
        // Resolve categories using cache and natural ID lookup
        saveCategory(productEntity);
        return true;
    }

    void saveCategory(ProductEntity pe) {
        Set<ProductCategoryEntity> managedCategories = pe.getCategories().stream()
            .map(productCategory -> findEntityByReference(productCategory.getClass(), productCategory.getCategory(),
                "category"))
            .collect(Collectors.toSet());
        if (managedCategories.isEmpty()) {
            return;
        }
        pe.setCategories(managedCategories);
    }

    private <T> T findEntityByReference(Class<T> reference, Object obj, String attributeName)
        throws ClassCastException {
        Session session = entityManager.unwrap(Session.class);
        return session.byNaturalId(reference)
            .using(attributeName, obj)
            .getReference();
    }

}
