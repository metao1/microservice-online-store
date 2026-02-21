package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.product.infrastructure.persistence.mapper.ProductEntityMapper;
import com.metao.book.shared.application.persistence.OffsetBasedPageRequest;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of ProductRepository
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final JpaCategoryRepository jpaCategoryRepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public void save(ProductAggregate product) {
        ProductEntity entity = productEntityMapper.toEntity(product);
        jpaProductRepository.save(entity);
    }

    @Override
    public boolean insertIfAbsent(ProductAggregate product) {
        ProductEntity entity = productEntityMapper.toEntity(product);
        int affected = jpaProductRepository.insertIfAbsent(entity);

        if (affected == 0) {
            return false;
        }

        if (product.getCategories().isEmpty()) {
            return true;
        }

        ProductEntity persisted = jpaProductRepository.getReferenceById(product.getId());
        product.getCategories().stream()
            .map(this::resolveCategoryEntity)
            .forEach(persisted::addCategory);
        return true;
    }

    @Override
    public Optional<ProductAggregate> findBySku(ProductSku productSku) {
        return jpaProductRepository.findById(productSku)
            .map(productEntityMapper::toDomain);
    }

    @Override
    public List<ProductAggregate> findBySkus(List<ProductSku> productSkus) {
        if (productSkus == null || productSkus.isEmpty()) {
            return List.of();
        }
        return jpaProductRepository.findAllById(productSkus)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductAggregate> findByCategory(CategoryName categoryName, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return jpaProductRepository.findByCategory(categoryName.value(), pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductAggregate> findByCategories(List<CategoryName> categoryNames, int offset, int limit) {
        List<String> names = categoryNames.stream()
            .map(CategoryName::value)
            .toList();
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return jpaProductRepository.findByCategories(names, pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductAggregate> searchByKeyword(String keyword, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return jpaProductRepository.searchByKeyword(keyword, pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsById(ProductSku productSku) {
        return jpaProductRepository.existsBySku(productSku);
    }

    @Override
    public void delete(ProductAggregate product) {
        // TODO to be implemented
    }

    @Override
    public void flush() {
        jpaProductRepository.flush();
    }

    @Override
    public boolean reduceVolumeAtomically(ProductSku sku, BigDecimal quantity) {
        return jpaProductRepository.decrementVolumeIfEnough(sku.value(), quantity) > 0;
    }

    private CategoryEntity resolveCategoryEntity(ProductCategory category) {
        String categoryName = category.getName().value();
        return jpaCategoryRepository.findByCategory(categoryName)
            .orElseGet(() -> new CategoryEntity(categoryName));
    }

}
