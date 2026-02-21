package com.metao.book.product.presentation;

import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.CreateProductDto;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.application.service.CreateProductResult;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductDomainService productDomainService;
    private final ProductApplicationMapper productMapper;

    @GetMapping(value = "/{sku}")
    public ProductDTO getProduct(@PathVariable @Valid @NotBlank ProductSku sku) {
        log.info("Getting product with SKU: {}", sku);
        ProductAggregate product = productDomainService.getProductBySku(sku);
        return productMapper.toDTO(product);
    }

    @GetMapping(value = "/by-skus")
    public List<ProductDTO> getProductsBySkus(@RequestParam("skus") List<String> skus) {
        var products = productDomainService.getProductsBySkus(skus);
        return products.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    @PostMapping
    public ResponseEntity<String> createProduct(
        @Valid @RequestBody CreateProductDto dto,
        @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        log.debug("Creating product: {}", dto);
        var createdTime = Instant.now();
        var command = new CreateProductCommand(
            dto.sku(),
            dto.title(),
            dto.description(),
            dto.imageUrl(),
            dto.price(),
            dto.currency(),
            dto.volume(),
            createdTime,
            dto.categories()
        );
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{sku}")
            .buildAndExpand(dto.sku())
            .toUri();

        CreateProductResult result = productDomainService.createProduct(command, idempotencyKey);
        if (result == null) {
            result = CreateProductResult.CREATED;
        }
        return switch (result) {
            case CREATED -> ResponseEntity.created(location).build();
            case REPLAYED -> ResponseEntity.ok().location(location).build();
            case ALREADY_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT).location(location).build();
        };
    }

    @PutMapping("/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ProductDTO updateProduct(
        @PathVariable String sku,
        @Valid @RequestBody UpdateProductCommand command
    ) {
        log.info("Updating product with SKU: {}", sku);
        // Ensure SKU in a path matches the command
        var updatedCommand = new UpdateProductCommand(
            sku, command.title(), command.description(), command.price(), command.currency()
        );
        var product = productDomainService.updateProduct(updatedCommand);
        return productMapper.toDTO(product);
    }

    @GetMapping("/category/{categoryName}")
    public List<ProductDTO> getProductsByCategory(
        @PathVariable CategoryName categoryName,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.info("Getting products by category: {}", categoryName);
        var productsByCategory = productDomainService.getProductsByCategory(categoryName, offset, limit);
        return productsByCategory.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(
        @RequestParam("keyword") String keyword,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.info("Searching products with keyword: {}", keyword);
        var products = productDomainService.searchProducts(keyword, offset, limit);
        return products.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    @GetMapping("/categories")
    public List<CategoryDTO> getCategories(
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        var categories = productDomainService.getCategories(offset, limit);
        return categories.stream()
            .map(category-> new CategoryDTO(category.getName().value()))
            .toList();
    }

    @GetMapping("/categories/{category}/subcategories")
    public List<CategoryDTO> getSubcategories(
        @PathVariable String category,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "12") int limit
    ) {
        var parent = CategoryName.of(category);
        var subcategories = productDomainService.getSubcategories(parent, offset, limit);
        return subcategories.stream()
            .map(sub -> new CategoryDTO(sub.getName().value()))
            .toList();
    }

    @GetMapping("/{sku}/related")
    public List<ProductDTO> getRelatedProducts(
        @PathVariable String sku,
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        log.info("Getting related products for SKU: {}", sku);
        var relatedProducts = productDomainService.getRelatedProducts(ProductSku.of(sku), limit);
        return relatedProducts.stream()
            .map(productMapper::toDTO)
            .toList();
    }

    @PostMapping("/{sku}/categories/{categoryName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignProductToCategory(
        @PathVariable String sku,
        @PathVariable String categoryName
    ) {
        log.info("Assigning product {} to category {}", sku, categoryName);
        productDomainService.assignProductToCategory(ProductSku.of(sku), CategoryName.of(categoryName));
    }

    @PostMapping("/{sku}/volume/reduce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reduceProductVolume(
        @PathVariable String sku,
        @RequestParam BigDecimal quantity
    ) {
        log.info("Reducing volume for product {} by {}", sku, quantity);
        productDomainService.reduceProductVolume(ProductSku.of(sku), Quantity.of(quantity));
    }

    @PostMapping("/{sku}/volume/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void increaseProductVolume(
        @PathVariable String sku,
        @RequestParam BigDecimal quantity
    ) {
        log.info("Increasing volume for product {} by {}", sku, quantity);
        productDomainService.increaseProductVolume(ProductSku.of(sku), Quantity.of(quantity));
    }
}
