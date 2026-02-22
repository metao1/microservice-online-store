package com.metao.book.product.application.service;

import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.domain.exception.CategoryNotFoundException;
import com.metao.book.product.domain.exception.IdempotencyKeyConflictException;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.infrastructure.persistence.repository.ProductCreateIdempotencyRepository;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.base.DomainEventPublisher;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Application service for Product operations - orchestrates domain services and repositories
 */
@Slf4j
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCreateIdempotencyRepository productCreateIdempotencyRepository;
    private final DomainEventPublisher eventPublisher;
    /**
     * Create a new product
     */
    public CreateProductResult createProduct(@Valid CreateProductCommand command) {
        return createProduct(command, UUID.randomUUID().toString());
    }

    public CreateProductResult createProduct(@Valid CreateProductCommand command, String idempotencyKey) {
        log.info("Creating product with SKU: {}", command.sku());

        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        // If no idempotency key is provided, create a unique one so the flow always continues
        if (normalizedIdempotencyKey == null) {
            normalizedIdempotencyKey = UUID.randomUUID().toString();
        }

        var claimResult = productCreateIdempotencyRepository.claim(normalizedIdempotencyKey, command.sku());
        if (claimResult == null) {
            claimResult = ProductCreateIdempotencyRepository.ClaimResult.CLAIMED;
        }
        switch (claimResult) {
            case REPLAY -> {
                log.info("Skipping duplicate create request for SKU {} with idempotency key {}", command.sku(),
                    normalizedIdempotencyKey);
                return CreateProductResult.REPLAYED;
            }
            case CONFLICT -> throw new IdempotencyKeyConflictException(
                "Idempotency key is already associated with a different SKU");
            case CLAIMED -> {
                // continue with create flow
            }
        }

        // Create domain objects
        var productSku = ProductSku.of(command.sku());
        var title = ProductTitle.of(command.title());
        var description = ProductDescription.of(command.description());
        var volume = Quantity.of(command.volume());
        var price = new Money(command.currency(), command.price());
        var imageUrl = ImageUrl.of(command.imageUrl());
        var createdTime = command.createdTime();

        var categories = new HashSet<ProductCategory>();
        // Create product aggregate
        if (command.categoryNames() != null && !command.categoryNames().isEmpty()) {
            for (String categoryName : command.categoryNames()) {
                if (categoryName == null) {
                    break;
                }
                CategoryName catName = CategoryName.of(categoryName);

                // Ensure a category exists
                ProductCategory category = categoryRepository.findByName(catName)
                    .orElseGet(() -> ProductCategory.of(catName));
                categories.add(category);
            }
        }
        var product = new ProductAggregate(
            productSku,
            title,
            description,
            volume,
            price,
            createdTime,
            createdTime,
            imageUrl,
            categories);

        boolean inserted = productRepository.insertIfAbsent(product);
        if (!inserted) {
            return CreateProductResult.ALREADY_EXISTS;
        }
        publishEvents(product);
        return CreateProductResult.CREATED;
    }

    /**
     * Update an existing product
     */
    public ProductAggregate updateProduct(@Valid UpdateProductCommand command) {
        var productSku = ProductSku.of(command.sku());
        var product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));
        product.updateTitle(ProductTitle.of(command.title()));

        product.updateDescription(ProductDescription.of(command.description()));
        Money newPrice = new Money(command.currency(), command.price());
        product.updatePrice(newPrice);

        productRepository.save(product);

        log.info("Product updated successfully with ID: {}", product.getId());
        publishEvents(product);
        return product;
    }

    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public ProductAggregate getProductBySku(@NotNull ProductSku sku) {
        log.debug("Getting product by SKU: {}", sku);
        return productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    /**
     * Get products by SKUs
     */
    @Transactional(readOnly = true)
    public List<ProductAggregate> getProductsBySkus(List<String> skus) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }
        List<ProductSku> productSkus = skus.stream()
            .filter(sku -> sku != null && !sku.isBlank())
            .map(ProductSku::of)
            .toList();
        if (productSkus.isEmpty()) {
            return List.of();
        }
        return productRepository.findBySkus(productSkus);
    }

    /**
     * Search products by keyword
     */
    @Transactional(readOnly = true)
    public List<ProductAggregate> searchProducts(String keyword, int offset, int limit) {
        if (keyword == null) {
            return List.of();
        }
        log.debug("Searching products with keyword: {}", keyword);

        List<ProductAggregate> products = productRepository.searchByKeyword(keyword, offset, limit);
        return products.stream()
            .toList();
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductAggregate> getProductsByCategory(CategoryName categoryName, int offset, int limit) {
        if (categoryName == null) {
            return List.of();
        }
        log.debug("Getting products by category: {}", categoryName);

        List<ProductAggregate> products = productRepository.findByCategory(categoryName, offset, limit);
        return products.stream()
            .toList();
    }

    /**
     * Get related products using domain service
     */
    @Transactional(readOnly = true)
    public List<ProductAggregate> getRelatedProducts(ProductSku sku, int limit) {
        if (sku == null) {
            return List.of();
        }
        log.debug("Getting related products for SKU: {}", sku);

        List<ProductAggregate> relatedProducts = findRelatedProducts(sku, limit);
        return relatedProducts.stream().toList();
    }

    /**
     * Assign product to category using domain service
     */
    public void assignProductToCategory(ProductSku productSku, CategoryName categoryName) {
        if (productSku == null || categoryName == null) {
            return;
        }
        log.info("Assigning product {} to category {}", productSku, categoryName);

        // Business rule validation and assignment
        if (!canAssignToCategory(productSku, categoryName)) {
            throw new IllegalStateException("Product cannot be assigned to more than 5 categories");
        }

        ProductAggregate product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        ProductCategory category = categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        product.addCategory(category);
        productRepository.save(product);
        publishEvents(product);
        log.info("Product {} assigned to category {} successfully", productSku, categoryName);
    }

    /**
     * Reduce product volume (for order processing)
     */
    public void reduceProductVolume(ProductSku sku, Quantity quantity) {
        if (sku == null || quantity == null) {
            return;
        }
        log.info("Reducing volume for product {} by {}", sku, quantity);

        ProductAggregate product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException(sku));

        product.reduceVolume(quantity);
        productRepository.save(product);
        publishEvents(product);
        log.info("Product volume reduced successfully for {}", sku);
    }

    /**
     * Reduce product volume with a single atomic SQL update.
     */
    public void reduceProductVolumeAtomically(String sku, BigDecimal quantity) {
        if (sku == null || quantity == null) {
            return;
        }
        log.info("Reducing volume atomically for product {} by {}", sku, quantity);

        ProductSku productSku = ProductSku.of(sku);
        Quantity.of(quantity);

        boolean updated = productRepository.reduceVolumeAtomically(productSku, quantity);
        if (!updated) {
            if (!productRepository.existsById(productSku)) {
                throw new ProductNotFoundException(productSku);
            }
            throw new IllegalStateException("Insufficient product volume for SKU " + sku);
        }
        log.info("Product volume reduced atomically for {}", sku);
    }

    /**
     * Increase product volume (for restocking)
     */
    public void increaseProductVolume(ProductSku sku, Quantity quantity) {
        if (sku == null || quantity == null) {
            return;
        }
        log.debug("Increasing volume for product {} by {}", sku, quantity);

        ProductAggregate product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException(sku));

        product.increaseVolume(quantity);
        productRepository.save(product);

        log.info("Product volume increased successfully for {}", sku);
    }

    public Set<ProductCategory> getCategories(int offset, int limit) {
        return categoryRepository.findAll(offset, limit);
    }

    private void publishEvents(ProductAggregate product) {
        List<DomainEvent> events = product.getDomainEvents();
        events.forEach(eventPublisher::publish);
        product.clearDomainEvents();
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
    }

    /**
     * Check if a product can be assigned to a category
     */
    public boolean canAssignToCategory(@NonNull ProductSku productSku, @NonNull CategoryName categoryName) {
        ProductAggregate product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        // Verify category exists and check if product can have more categories
        categoryRepository.findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        // Business rule: A product can only be in maximum 5 categories
        return product.getCategories().size() < 5;
    }

    /**
     * Check if a product is unique by SKU
     */
    public Boolean isProductUnique(@NotEmpty String sku) {
        ProductSku productSku = ProductSku.of(sku);
        return productRepository.findBySku(productSku).isEmpty();
    }

    /**
     * Find related products by shared categories
     */
    public List<ProductAggregate> findRelatedProducts(@NonNull ProductSku productSku, int limit) {
        ProductAggregate product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        if (product.getCategories().isEmpty()) {
            return List.of();
        }

        // Get category names from the product
        List<CategoryName> categoryNames = product.getCategories().stream()
            .map(ProductCategory::getName)
            .toList();

        // Find products in same categories, excluding the original product
        return productRepository.findByCategories(categoryNames, 0, limit)
            .stream()
            .filter(p -> !p.getId().equals(productSku))
            .toList();
    }
}
