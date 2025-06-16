package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductId;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.product.infrastructure.persistence.mapper.ProductEntityMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of ProductRepository
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public Product save(Product product) {
        ProductEntity entity = productEntityMapper.toEntity(product);
        ProductEntity savedEntity = jpaProductRepository.save(entity);
        return productEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return jpaProductRepository.findByAsin(productId.value())
            .map(productEntityMapper::toDomain);
    }

    @Override
    public Optional<Product> findByAsin(String asin) {
        return jpaProductRepository.findByAsin(asin)
            .map(productEntityMapper::toDomain);
    }

    @Override
    public List<Product> findByCategory(CategoryName categoryName, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaProductRepository.findByCategory(categoryName.getValue(), pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<Product> findByCategories(List<CategoryName> categoryNames, int offset, int limit) {
        List<String> names = categoryNames.stream()
            .map(CategoryName::getValue)
            .toList();
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaProductRepository.findByCategories(names, pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<Product> searchByKeyword(String keyword, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaProductRepository.searchByKeyword(keyword, pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsById(ProductId productId) {
        return jpaProductRepository.existsByAsin(productId.value());
    }

    @Override
    public void delete(Product product) {
        jpaProductRepository.findByAsin(product.getId().value())
            .ifPresent(jpaProductRepository::delete);
    }

    @Override
    public List<Product> findAll(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaProductRepository.findAll(pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public long count() {
        return jpaProductRepository.count();
    }
}
