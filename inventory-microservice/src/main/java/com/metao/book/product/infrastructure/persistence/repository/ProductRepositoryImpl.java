package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.product.infrastructure.persistence.mapper.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Infrastructure implementation of ProductRepository
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public void save(Product product) {
        ProductEntity entity = productEntityMapper.toEntity(product);
        jpaProductRepository.save(entity);
    }

    @Override
    public Optional<Product> findBySku(ProductSku productSku) {
        return jpaProductRepository.findBySkuForUpdate(productSku)
            .map(productEntityMapper::toDomain);
    }

    @Override
    public List<Product> findByCategory(CategoryName categoryName, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaProductRepository.findByCategory(categoryName.value(), pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<Product> findByCategories(List<CategoryName> categoryNames, int offset, int limit) {
        List<String> names = categoryNames.stream()
            .map(CategoryName::value)
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
    public boolean existsById(ProductSku productSku) {
        return jpaProductRepository.existsBySku(productSku);
    }

    @Override
    public void delete(Product product) {
        // TODO to be implemented
    }

    @Override
    public List<Product> findAll(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaProductRepository.findAll(pageable)
            .stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }

}
