package com.metao.book.product.infrastructure.persistence.repository;

import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.infrastructure.persistence.mapper.CategoryEntityMapper;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Set<ProductCategory> findAll(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaCategoryRepository.findAll(pageable)
            .stream()
            .map(categoryEntityMapper::toDomain)
            .collect(Collectors.toSet());
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
        jpaCategoryRepository.findByCategory(category.getName().value())
            .ifPresent(jpaCategoryRepository::delete);
    }
}
