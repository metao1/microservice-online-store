package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import com.metao.book.product.infrastructure.persistence.mapper.CategoryEntityMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of CategoryRepository
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Override
    public ProductCategory save(ProductCategory category) {
        CategoryEntity entity = categoryEntityMapper.toEntity(category);
        CategoryEntity savedEntity = jpaCategoryRepository.save(entity);
        return categoryEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductCategory> findById(CategoryId categoryId) {
        return jpaCategoryRepository.findById(categoryId.value())
            .map(categoryEntityMapper::toDomain);
    }

    @Override
    public Optional<ProductCategory> findByName(CategoryName categoryName) {
        return jpaCategoryRepository.findByCategory(categoryName.value())
            .map(categoryEntityMapper::toDomain);
    }

    @Override
    public boolean existsByName(CategoryName categoryName) {
        return jpaCategoryRepository.existsByCategory(categoryName.value());
    }

    @Override
    public void delete(ProductCategory category) {
        jpaCategoryRepository.findById(category.getId().value())
            .ifPresent(jpaCategoryRepository::delete);
    }
}
