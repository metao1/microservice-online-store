package com.metao.book.product.application.service;

import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductSku;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.domain.service.ProductDomainService;
import com.metao.book.shared.domain.financial.Money;
import jakarta.validation.Valid;
import java.util.List;
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
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDomainService productDomainService;

    /**
     * Create a new product
     */
    public void createProduct(@Valid CreateProductCommand command) {
        log.info("Creating product with SKU: {}", command.sku());

        // Use domain service to check uniqueness
        if (!productDomainService.isProductUnique(command.sku())) {
            throw new IllegalArgumentException("Product with SKU " + command.sku() + " already exists");
        }

        // Create domain objects
        var productId = ProductSku.of(command.sku());
        var title = ProductTitle.of(command.title());
        var description = ProductDescription.of(command.description());
        var volume = ProductVolume.of(command.volume());
        var price = new Money(command.currency(), command.price());
        var imageUrl = ImageUrl.of(command.imageUrl());

        // Create product aggregate
        var product = new Product(productId, title, description, volume, price, imageUrl);

        if (command.categoryNames() != null) {
            for (String categoryName : command.categoryNames()) {
                if (categoryName == null) continue;
                CategoryName catName = CategoryName.of(categoryName);

                // Ensure category exists
                ProductCategory category = categoryRepository.findByName(catName)
                    .orElseGet(() -> ProductCategory.of(catName));
                product.addCategory(category);
            }
        }

        productRepository.save(product);
    }

    //TODO return DTO instead decouple controller's from internal aggregate logic
    /**
     * Update an existing product
     */
    public Product updateProduct(@Valid UpdateProductCommand command) {
        log.info("Updating product with SKU: {}", command.sku());

        ProductSku productSku = ProductSku.of(command.sku());
        Product product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        if (command.title() != null) {
            product.updateTitle(ProductTitle.of(command.title()));
        }
        if (command.description() != null) {
            product.updateDescription(ProductDescription.of(command.description()));
        }
        if (command.price() != null && command.currency() != null) {
            Money newPrice = new Money(command.currency(), command.price());
            product.updatePrice(newPrice);
        }

        productRepository.save(product);

        log.info("Product updated successfully with ID: {}", product.getId());

        return product;
    }

    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or empty.");
        }
        log.debug("Getting product by SKU: {}", sku);
        ProductSku productSku = ProductSku.of(sku);
        return productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));
    }

    /**
     * Search products by keyword
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword, int offset, int limit) {
        if (keyword == null) return List.of();
        log.debug("Searching products with keyword: {}", keyword);

        List<Product> products = productRepository.searchByKeyword(keyword, offset, limit);
        return products.stream()
            .toList();
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String categoryName, int offset, int limit) {
        if (categoryName == null) return List.of();
        log.debug("Getting products by category: {}", categoryName);

        CategoryName catName = CategoryName.of(categoryName);
        List<Product> products = productRepository.findByCategory(catName, offset, limit);
        return products.stream()
            .toList();
    }

    /**
     * Get related products using domain service
     */
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(String sku, int limit) {
        if (sku == null) {
            return List.of();
        }
        log.debug("Getting related products for SKU: {}", sku);

        ProductSku productSku = ProductSku.of(sku);
        List<Product> relatedProducts = productDomainService.findRelatedProducts(productSku, limit);
        return relatedProducts.stream()
            .toList();
    }

    /**
     * Assign product to category using domain service
     */
    public void assignProductToCategory(String sku, String categoryName) {
        if (sku == null || categoryName == null) {
            return;
        }
        log.info("Assigning product {} to category {}", sku, categoryName);

        ProductSku productSku = ProductSku.of(sku);
        CategoryName catName = CategoryName.of(categoryName);

        // Business rule validation and assignment
        productDomainService.assignProductToCategory(productSku, catName);

        log.info("Product {} assigned to category {} successfully", sku, categoryName);
    }

    /**
     * Reduce product volume (for order processing)
     */
    public void reduceProductVolume(String sku, java.math.BigDecimal quantity) {
        if (sku == null || quantity == null) {
            return;
        }
        log.info("Reducing volume for product {} by {}", sku, quantity);

        ProductSku productSku = ProductSku.of(sku);
        Product product = productRepository.findBySku(productSku)
            .orElseThrow(() -> new ProductNotFoundException(productSku));

        ProductVolume reduction = ProductVolume.of(quantity);
        product.reduceVolume(reduction);
        productRepository.save(product);

        log.info("Product volume reduced successfully for {}", sku);
    }

    /**
     * Increase product volume (for restocking)
     */
    public void increaseProductVolume(String sku, java.math.BigDecimal quantity) {
        if (sku == null || quantity == null) {
            return;
        }
        log.info("Increskug volume for product {} by {}", sku, quantity);

        ProductSku productSKU = ProductSku.of(sku);
        Product product = productRepository.findBySku(productSKU)
            .orElseThrow(() -> new ProductNotFoundException(productSKU));

        ProductVolume increase = ProductVolume.of(quantity);
        product.increaseVolume(increase);
        productRepository.save(product);

        log.info("Product volume increased successfully for {}", sku);
    }
}