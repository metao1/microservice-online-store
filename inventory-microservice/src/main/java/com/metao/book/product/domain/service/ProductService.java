package com.metao.book.product.domain.service;

import com.metao.book.product.domain.Product;
import com.metao.book.product.domain.category.ProductCategory;
import com.metao.book.product.domain.dto.ProductDTO;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.mapper.ProductMapper;
import com.metao.book.product.infrastructure.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @PersistenceContext
    private final EntityManager entityManager;


    public Optional<Product> getProductByAsin(String asin) throws ProductNotFoundException{
        var productEntity = productRepository.findByAsin(asin)
            .orElseThrow(() -> new ProductNotFoundException(asin));
        return Optional.ofNullable(productEntity);
    }

    public List<Product> getProductsByCategories(int limit, int offset, List<String> categoryNames) {
        if (CollectionUtils.isEmpty(categoryNames)) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(offset, limit);
        return productRepository.findAllByCategories(categoryNames, pageable);
    }

    public boolean saveProduct(Product product) {
        Product existingProduct = findEntityByReference(Product.class, product.getAsin(), "asin");
        if (existingProduct != null) {
            return false;
        }
        // product needs to be saved first to have a managed state, when categories are added later
        // the product will be updated the hibernates is able to track the changes and hence saves the categories in
        // the product relationship. Otherwise, if categories are added directly before the product is saved, the
        // categories will be in transient state and will not be tracked by hibernates.
        // Resolve categories using cache and natural ID lookup
        saveCategory(product);
        productRepository.save(product);
        return true;
    }

    void saveCategory(Product pe) {
        if (CollectionUtils.isEmpty(pe.getCategories())) {
            return;
        }
        final Set<ProductCategory> managedCategories = findEntityByReference(pe);
        if (!managedCategories.isEmpty()) {
            pe.getCategories().removeAll(managedCategories);
        }
    }

    private <T> T findEntityByReference(Class<T> reference, Object obj, String attributeName)
        throws ClassCastException {
        Session session = entityManager.unwrap(Session.class);
        return session.byNaturalId(reference)
            .using(attributeName, obj)
            .getReference();
    }

    // TODO use the above method instead
    Set<ProductCategory> findEntityByReference(Product product)
        throws ClassCastException {
        Session session = entityManager.unwrap(Session.class);
        return product.getCategories()
            .stream()
            .map(category -> session.byNaturalId(ProductCategory.class)
                .using("category", category.getCategory())
                .loadOptional()
            ).<ProductCategory>mapMulti(Optional::ifPresent)
            .collect(Collectors.toSet());
    }

    public List<ProductDTO> searchProductsByKeyword(String keyword, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        List<Product> products = productRepository.searchByKeyword(keyword, pageable);
        return products.stream().map(ProductMapper::toDto).collect(Collectors.toList());
    }
}
