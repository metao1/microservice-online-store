package com.metao.book.product.infrastructure.persistence.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.infrastructure.persistence.entity.CategoryEntity;
import com.metao.book.product.infrastructure.persistence.entity.ProductEntity;
import com.metao.book.product.infrastructure.persistence.mapper.ProductEntityMapper;
import com.metao.book.shared.application.persistence.OffsetBasedPageRequest;
import com.metao.book.shared.domain.product.ProductSku;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of ProductRepository
 */
@Repository
@Transactional
@Slf4j
@RequiredArgsConstructor
@Observed(name = "product.persistence.repository", contextualName = "product-repository")
public class ProductRepositoryImpl implements ProductRepository {

    private static final int CATEGORY_ID_CACHE_MAXIMUM_SIZE = 2_048;

    private final JpaProductRepository jpaProductRepository;
    private final JpaCategoryRepository jpaCategoryRepository;
    private final EntityManager entityManager;
    private final ProductEntityMapper productEntityMapper;
    private final Cache<String, Optional<String>> categoryIdCache = Caffeine.newBuilder()
        .maximumSize(CATEGORY_ID_CACHE_MAXIMUM_SIZE)
        .expireAfterAccess(Duration.ofHours(6))
        .build();

    @PostConstruct
    void warmCategoryIdCache() {
        jpaCategoryRepository.findAll().forEach(category ->
            categoryIdCache.put(normalizeCategoryCacheKey(category.getCategory()), Optional.of(category.getId()))
        );
        log.info("Warmed category id cache with {} categories", categoryIdCache.estimatedSize());
    }

    @Override
    public void save(ProductAggregate product) {
        ProductEntity source = productEntityMapper.toEntity(product);
        ProductEntity managed = entityManager.find(ProductEntity.class, product.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (managed == null) {
            entityManager.persist(source);
        } else {
            managed.updateFrom(source);
        }
        invalidateCategoryCacheEntries(product.getCategories());
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
        invalidateCategoryCacheEntries(product.getCategories());
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
        Optional<String> categoryId = resolveCategoryId(categoryName.value());
        if (categoryId.isEmpty()) {
            return List.of();
        }
        List<ProductSku> skus = jpaProductRepository.findSkusByCategoryId(categoryId.get(), pageable);
        return loadProductsWithCategoriesInOrder(skus);
    }

    @Override
    public List<ProductAggregate> findByCategories(List<CategoryName> categoryNames, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        var names = categoryNames.stream()
            .map(CategoryName::value)
            .map(name -> name.toLowerCase(Locale.ROOT))
            .toList();
        List<String> categoryIds = resolveCategoryIds(names);
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        List<ProductSku> skus = jpaProductRepository.findSkusByCategoryIds(categoryIds, pageable);
        return loadProductsWithCategoriesInOrder(skus);
    }

    @Override
    public List<ProductAggregate> searchByKeyword(String keyword, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        List<ProductSku> skus = jpaProductRepository.searchSkusByKeyword(keyword, pageable);
        return loadProductsWithCategoriesInOrder(skus);
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
    public boolean reduceVolumeAtomically(ProductSku sku, BigDecimal quantity) {
        return jpaProductRepository.decrementVolumeIfEnough(sku.value(), quantity) > 0;
    }

    private CategoryEntity resolveCategoryEntity(ProductCategory category) {
        String normalizedCategoryName = normalizeCategoryCacheKey(category.getName().value());
        return findCategoryByNaturalId(normalizedCategoryName)
            .filter(existingCategory -> categoryStillExists(existingCategory.getId(), normalizedCategoryName))
            .map(existingCategory -> {
                categoryIdCache.put(
                    normalizeCategoryCacheKey(existingCategory.getCategory()),
                    Optional.of(existingCategory.getId())
                );
                return existingCategory;
            })
            .orElseGet(() -> new CategoryEntity(category.getName()));
    }

    private Optional<String> resolveCategoryId(String categoryName) {
        String normalizedCategoryName = normalizeCategoryCacheKey(categoryName);
        Optional<String> cachedCategoryId = categoryIdCache.getIfPresent(normalizedCategoryName);
        if (cachedCategoryId != null) {
            return cachedCategoryId;
        }

        Optional<String> categoryId = findCategoryByNaturalId(normalizedCategoryName)
            .map(CategoryEntity::getId);
        categoryIdCache.put(normalizedCategoryName, categoryId);
        return categoryId;
    }

    private List<String> resolveCategoryIds(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return List.of();
        }

        Map<String, Optional<String>> resolvedCategoryIds = new HashMap<>();
        for (String categoryName : categoryNames) {
            String normalizedCategoryName = normalizeCategoryCacheKey(categoryName);
            Optional<String> cachedCategoryId = categoryIdCache.getIfPresent(normalizedCategoryName);
            if (cachedCategoryId != null && cachedCategoryId.isPresent()) {
                resolvedCategoryIds.put(normalizedCategoryName, cachedCategoryId);
            }
        }

        List<String> missingCategoryNames = categoryNames.stream()
            .map(this::normalizeCategoryCacheKey)
            .filter(categoryName -> !resolvedCategoryIds.containsKey(categoryName))
            .distinct()
            .toList();

        if (!missingCategoryNames.isEmpty()) {
            Map<String, String> loadedCategoryIds = jpaCategoryRepository.findAll().stream()
                .filter(category -> missingCategoryNames.contains(category.getCategory()))
                .collect(Collectors.toMap(CategoryEntity::getCategory, CategoryEntity::getId));

            for (String missingCategoryName : missingCategoryNames) {
                Optional<String> categoryId = Optional.ofNullable(loadedCategoryIds.get(missingCategoryName));
                categoryIdCache.put(missingCategoryName, categoryId);
                resolvedCategoryIds.put(missingCategoryName, categoryId);
            }
        }

        return categoryNames.stream()
            .map(this::normalizeCategoryCacheKey)
            .map(resolvedCategoryIds::get)
            .filter(Objects::nonNull)
            .flatMap(Optional::stream)
            .distinct()
            .toList();
    }

    private void invalidateCategoryCacheEntries(Collection<ProductCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        categories.stream()
            .map(ProductCategory::getName)
            .map(CategoryName::value)
            .map(this::normalizeCategoryCacheKey)
            .forEach(categoryIdCache::invalidate);
    }

    private String normalizeCategoryCacheKey(String categoryName) {
        return categoryName == null ? null : categoryName.toLowerCase(Locale.ROOT).trim();
    }

    private boolean categoryStillExists(String categoryId, String normalizedCategoryName) {
        if (categoryId == null || !jpaCategoryRepository.existsById(categoryId)) {
            categoryIdCache.invalidate(normalizedCategoryName);
            return false;
        }
        return true;
    }

    private Optional<CategoryEntity> findCategoryByNaturalId(String categoryName) {
        Session session = entityManager.unwrap(Session.class);
        return session.bySimpleNaturalId(CategoryEntity.class)
            .loadOptional(categoryName);
    }

    private List<ProductAggregate> loadProductsWithCategoriesInOrder(List<ProductSku> skus) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }

        ProductReadModel readModel = loadProductReadModel(skus);

        return skus.stream()
            .map(readModel.productsBySku()::get)
            .filter(Objects::nonNull)
            .map(entity -> productEntityMapper.toDomain(
                entity,
                readModel.categoriesBySku().getOrDefault(entity.getSku(), Set.of())
            ))
            .toList();
    }

    private ProductReadModel loadProductReadModel(List<ProductSku> skus) {
        Map<ProductSku, ProductEntity> productsBySku = new LinkedHashMap<>();
        jpaProductRepository.findAllById(skus)
            .forEach(entity -> productsBySku.put(entity.getSku(), entity));

        Map<ProductSku, Set<ProductCategory>> categoriesBySku = new HashMap<>();
        jpaProductRepository.findCategoryRowsBySkuIn(skus)
            .forEach(row -> categoriesBySku
                .computeIfAbsent(row.getSku(), ignored -> new LinkedHashSet<>())
                .add(productEntityMapper.toDomain(row.getCategoryId(), row.getCategoryName())));

        return new ProductReadModel(productsBySku, categoriesBySku);
    }

    private record ProductReadModel(
        Map<ProductSku, ProductEntity> productsBySku,
        Map<ProductSku, Set<ProductCategory>> categoriesBySku
    ) {
    }

}
