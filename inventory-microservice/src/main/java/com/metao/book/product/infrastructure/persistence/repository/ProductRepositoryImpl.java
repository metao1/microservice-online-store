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
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
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

    private static final long SLOW_REPOSITORY_CALL_THRESHOLD_MS = 250L;
    private static final int CATEGORY_ID_CACHE_MAXIMUM_SIZE = 2_048;

    private final JpaProductRepository jpaProductRepository;
    private final JpaCategoryRepository jpaCategoryRepository;
    private final ProductEntityMapper productEntityMapper;
    private final ObservationRegistry observationRegistry;
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
        ProductEntity entity = productEntityMapper.toEntity(product);
        jpaProductRepository.save(entity);
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
        long startedAt = System.nanoTime();
        TimedResult<Optional<String>> categoryResult = observeTimed(
            "product.persistence.find-by-category.find-category",
            "find-category-by-name",
            () -> resolveCategoryId(categoryName.value())
        );
        if (categoryResult.value().isEmpty()) {
            return List.of();
        }
        TimedResult<List<ProductSku>> skusResult = observeTimed(
            "product.persistence.find-by-category.find-skus",
            "find-category-skus",
            () -> jpaProductRepository.findSkusByCategoryId(categoryResult.value().get(), pageable)
        );
        TimedLoadResult loadResult = loadProductsWithCategoriesInOrder(skusResult.value(), "find-by-category");
        logIfSlow(
            "findByCategory",
            startedAt,
            skusResult.value().size(),
            loadResult.products().size(),
            categoryResult.elapsedMs(),
            skusResult.elapsedMs(),
            loadResult.fetchProductsMs(),
            loadResult.mapDomainMs()
        );
        return loadResult.products();
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
        long startedAt = System.nanoTime();
        TimedResult<List<ProductSku>> skusResult = observeTimed(
            "product.persistence.find-by-categories.find-skus",
            "find-categories-skus",
            () -> jpaProductRepository.findSkusByCategoryIds(categoryIds, pageable)
        );
        TimedLoadResult loadResult = loadProductsWithCategoriesInOrder(skusResult.value(), "find-by-categories");
        logIfSlow(
            "findByCategories",
            startedAt,
            skusResult.value().size(),
            loadResult.products().size(),
            0L,
            skusResult.elapsedMs(),
            loadResult.fetchProductsMs(),
            loadResult.mapDomainMs()
        );
        return loadResult.products();
    }

    @Override
    public List<ProductAggregate> searchByKeyword(String keyword, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        long startedAt = System.nanoTime();
        TimedResult<List<ProductSku>> skusResult = observeTimed(
            "product.persistence.search-by-keyword.find-skus",
            "search-keyword-skus",
            () -> jpaProductRepository.searchSkusByKeyword(keyword, pageable)
        );
        TimedLoadResult loadResult = loadProductsWithCategoriesInOrder(skusResult.value(), "search-by-keyword");
        logIfSlow(
            "searchByKeyword",
            startedAt,
            skusResult.value().size(),
            loadResult.products().size(),
            0L,
            skusResult.elapsedMs(),
            loadResult.fetchProductsMs(),
            loadResult.mapDomainMs()
        );
        return loadResult.products();
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
        return jpaCategoryRepository.findByCategory(category.getName().value())
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

        Optional<String> categoryId = jpaCategoryRepository.findByCategory(normalizedCategoryName)
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
            if (cachedCategoryId != null) {
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

    private TimedLoadResult loadProductsWithCategoriesInOrder(List<ProductSku> skus, String operation) {
        if (skus == null || skus.isEmpty()) {
            return new TimedLoadResult(List.of(), 0L, 0L);
        }

        TimedResult<ProductReadModel> readModelResult = observeTimed(
            "product.persistence." + operation + ".fetch-products",
            "fetch-products-with-categories",
            () -> loadProductReadModel(skus)
        );

        TimedResult<List<ProductAggregate>> productsResult = observeTimed(
            "product.persistence." + operation + ".map-domain",
            "map-products-to-domain",
            () -> skus.stream()
                .map(readModelResult.value().productsBySku()::get)
                .filter(Objects::nonNull)
                .map(entity -> productEntityMapper.toDomain(
                    entity,
                    readModelResult.value().categoriesBySku().getOrDefault(entity.getSku(), Set.of())
                ))
                .toList()
        );
        return new TimedLoadResult(
            productsResult.value(),
            readModelResult.elapsedMs(),
            productsResult.elapsedMs()
        );
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

    private <T> T observe(String name, String contextualName, java.util.function.Supplier<T> supplier) {
        return Observation.createNotStarted(name, observationRegistry)
            .contextualName(contextualName)
            .observe(supplier);
    }

    private <T> TimedResult<T> observeTimed(String name, String contextualName, java.util.function.Supplier<T> supplier) {
        long startedAt = System.nanoTime();
        T value = observe(name, contextualName, supplier);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        return new TimedResult<>(value, elapsedMs);
    }

    private void logIfSlow(
        String operation,
        long startedAt,
        int skuCount,
        int resultCount,
        long findCategoryMs,
        long findSkusMs,
        long fetchProductsMs,
        long mapDomainMs
    ) {
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        if (elapsedMs >= SLOW_REPOSITORY_CALL_THRESHOLD_MS) {
            log.warn(
                "Slow product repository call: operation={}, elapsedMs={}, skuCount={}, resultCount={}, findCategoryMs={}, findSkusMs={}, fetchProductsMs={}, mapDomainMs={}",
                operation,
                elapsedMs,
                skuCount,
                resultCount,
                findCategoryMs,
                findSkusMs,
                fetchProductsMs,
                mapDomainMs
            );
        }
    }

    private record TimedResult<T>(T value, long elapsedMs) {
    }

    private record TimedLoadResult(List<ProductAggregate> products, long fetchProductsMs, long mapDomainMs) {
    }

}
