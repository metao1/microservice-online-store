package com.metao.book.product.application.service;

import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.domain.exception.ProductNotFoundException;
import com.metao.book.product.domain.model.aggregate.Product;
import com.metao.book.product.domain.model.entity.ProductCategory;
import com.metao.book.product.domain.model.valueobject.CategoryId;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.product.domain.model.valueobject.ImageUrl;
import com.metao.book.product.domain.model.valueobject.ProductDescription;
import com.metao.book.product.domain.model.valueobject.ProductId;
import com.metao.book.product.domain.model.valueobject.ProductTitle;
import com.metao.book.product.domain.model.valueobject.ProductVolume;
import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.domain.service.ProductDomainService;
import com.metao.book.shared.domain.financial.Money;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for Product operations - orchestrates domain services and repositories
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDomainService productDomainService;
    private final ProductApplicationMapper productMapper;

    /**
     * Create a new product
     */
    public ProductDTO createProduct(CreateProductCommand command) {
        log.info("Creating product with ASIN: {}", command.asin());

        // Use domain service to check uniqueness
        if (!productDomainService.isProductUnique(command.asin())) {
            throw new IllegalArgumentException("Product with ASIN " + command.asin() + " already exists");
        }

        // Create domain objects
        ProductId productId = ProductId.of(command.asin());
        ProductTitle title = ProductTitle.of(command.title());
        ProductDescription description = ProductDescription.of(command.description());
        ProductVolume volume = ProductVolume.of(command.volume());
        Money price = new Money(command.currency(), command.price());
        ImageUrl imageUrl = ImageUrl.of(command.imageUrl());

        // Create product aggregate
        Product product = new Product(productId, title, description, volume, price, imageUrl);

        // Add categories
        if (command.categoryNames() != null) {
            for (String categoryName : command.categoryNames()) {
                CategoryName catName = CategoryName.of(categoryName);

                // Ensure category exists
                ProductCategory category = categoryRepository.findByName(catName)
                    .orElseGet(() -> {
                        ProductCategory newCategory = new ProductCategory(
                            CategoryId.of(System.currentTimeMillis()), // Simple ID generation
                            catName
                        );
                        return categoryRepository.save(newCategory);
                    });

                product.addCategory(category);
            }
        }

        // Save product
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Update an existing product
     */
    public ProductDTO updateProduct(UpdateProductCommand command) {
        log.info("Updating product with ASIN: {}", command.asin());

        ProductId productId = ProductId.of(command.asin());
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        // Update product using domain methods
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

        Product savedProduct = productRepository.save(product);

        log.info("Product updated successfully with ID: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Get product by ASIN
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductByAsin(String asin) {
        log.debug("Getting product by ASIN: {}", asin);

        return productRepository.findByAsin(asin)
            .map(productMapper::toDTO);
    }

    /**
     * Search products by keyword
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword, int offset, int limit) {
        log.debug("Searching products with keyword: {}", keyword);

        List<Product> products = productRepository.searchByKeyword(keyword, offset, limit);
        return products.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String categoryName, int offset, int limit) {
        log.debug("Getting products by category: {}", categoryName);

        CategoryName catName = CategoryName.of(categoryName);
        List<Product> products = productRepository.findByCategory(catName, offset, limit);
        return products.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    /**
     * Get related products using domain service
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getRelatedProducts(String asin, int limit) {
        log.debug("Getting related products for ASIN: {}", asin);

        ProductId productId = ProductId.of(asin);
        List<Product> relatedProducts = productDomainService.findRelatedProducts(productId, limit);
        return relatedProducts.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    /**
     * Assign product to category using domain service
     */
    public void assignProductToCategory(String asin, String categoryName) {
        log.info("Assigning product {} to category {}", asin, categoryName);

        ProductId productId = ProductId.of(asin);
        CategoryName catName = CategoryName.of(categoryName);

        // Use domain service for business rule validation
        productDomainService.assignProductToCategory(productId, catName);

        log.info("Product {} assigned to category {} successfully", asin, categoryName);
    }

    /**
     * Reduce product volume (for order processing)
     */
    public void reduceProductVolume(String asin, java.math.BigDecimal quantity) {
        log.info("Reducing volume for product {} by {}", asin, quantity);

        ProductId productId = ProductId.of(asin);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductVolume reduction = ProductVolume.of(quantity);
        product.reduceVolume(reduction);

        productRepository.save(product);

        log.info("Product volume reduced successfully for {}", asin);
    }

    /**
     * Increase product volume (for restocking)
     */
    public void increaseProductVolume(String asin, java.math.BigDecimal quantity) {
        log.info("Increasing volume for product {} by {}", asin, quantity);

        ProductId productId = ProductId.of(asin);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductVolume increase = ProductVolume.of(quantity);
        product.increaseVolume(increase);

        productRepository.save(product);

        log.info("Product volume increased successfully for {}", asin);
    }
}
