package com.metao.book.product.presentation;

import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.dto.UpdateProductCommand;
import com.metao.book.product.application.service.ProductApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductApplicationService productApplicationService;

    @GetMapping(value = "/{asin}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String asin) {
        log.info("Getting product with ASIN: {}", asin);

        Optional<ProductDTO> product = productApplicationService.getProductByAsin(asin);
        return product.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@Valid @RequestBody CreateProductCommand command) {
        log.info("Creating product with ASIN: {}", command.asin());
        return productApplicationService.createProduct(command);
    }

    @PutMapping("/{asin}")
    public ProductDTO updateProduct(
        @PathVariable String asin,
        @Valid @RequestBody UpdateProductCommand command
    ) {
        log.info("Updating product with ASIN: {}", asin);
        // Ensure ASIN in path matches command
        UpdateProductCommand updatedCommand = new UpdateProductCommand(
            asin, command.title(), command.description(), command.price(), command.currency()
        );
        return productApplicationService.updateProduct(updatedCommand);
    }

    @GetMapping("/category/{categoryName}")
    public List<ProductDTO> getProductsByCategory(
        @PathVariable String categoryName,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.info("Getting products by category: {}", categoryName);
        return productApplicationService.getProductsByCategory(categoryName, offset, limit);
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(
        @RequestParam("keyword") String keyword,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.info("Searching products with keyword: {}", keyword);
        return productApplicationService.searchProducts(keyword, offset, limit);
    }

    @GetMapping("/{asin}/related")
    public List<ProductDTO> getRelatedProducts(
        @PathVariable String asin,
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        log.info("Getting related products for ASIN: {}", asin);
        return productApplicationService.getRelatedProducts(asin, limit);
    }

    @PostMapping("/{asin}/categories/{categoryName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignProductToCategory(
        @PathVariable String asin,
        @PathVariable String categoryName
    ) {
        log.info("Assigning product {} to category {}", asin, categoryName);
        productApplicationService.assignProductToCategory(asin, categoryName);
    }

    @PostMapping("/{asin}/volume/reduce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reduceProductVolume(
        @PathVariable String asin,
        @RequestParam java.math.BigDecimal quantity
    ) {
        log.info("Reducing volume for product {} by {}", asin, quantity);
        productApplicationService.reduceProductVolume(asin, quantity);
    }

    @PostMapping("/{asin}/volume/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void increaseProductVolume(
        @PathVariable String asin,
        @RequestParam java.math.BigDecimal quantity
    ) {
        log.info("Increasing volume for product {} by {}", asin, quantity);
        productApplicationService.increaseProductVolume(asin, quantity);
    }
}
