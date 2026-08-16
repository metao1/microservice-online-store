package com.metao.book.product.infrastructure.factory.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.dto.CreateProductCommand;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.service.CreateProductResult;
import com.metao.book.product.application.usecase.ProductUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Transactional
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "generator")
public class ProductGenerator {

    private final ProductUseCase productUseCase;
    private final ObjectMapper dtoMapper;
    private final Resource resource;

    public ProductGenerator(
        ProductUseCase productUseCase,
        ObjectMapper dtoMapper,
        @Value("${product.seed.resource:classpath:data/products.txt}") Resource resource
    ) {
        this.productUseCase = productUseCase;
        this.dtoMapper = dtoMapper;
        this.resource = resource;
    }

    @PostConstruct
    void validateSeedResource() {
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Product seed resource is not readable: " + resource.getDescription());
        }
    }

    /**
     * Waits for the {@link ReadinessState#ACCEPTING_TRAFFIC} and starts task execution
     *
     * @param event The {@link AvailabilityChangeEvent}
     */
    @EventListener
    public void run(AvailabilityChangeEvent<ReadinessState> event) {
        log.info("Application ReadinessState changed to: {}", event.getState());
        if (event.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
            CompletableFuture.runAsync(this::loadProducts);
        }
    }

    @Transactional
    public void loadProducts() {
        log.info("importing products data from resources");
        final List<ProductDTO> parsedProducts;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            parsedProducts = reader.lines()
                .map(this::parseProduct)
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException e) {
            log.error("Error reading products file", e);
            return;
        }

        List<CreateProductCommand> products = parsedProducts.stream()
                .filter(this::hasRequiredFields)
                .map(this::toCommand)
                .toList();
        int invalidProductCount = parsedProducts.size() - products.size();

        log.info("Parsed {} valid products, skipped {} incomplete products", products.size(), invalidProductCount);

        // Save in batches of 50
        int batchSize = 50;
        int savedCount = 0;
        int skippedDuplicateCount = 0;
        int processed = 0;
        for (CreateProductCommand product : products) {
            try {
                var createProductResult = productUseCase.createProduct(product);
                if (createProductResult.equals(CreateProductResult.ALREADY_EXISTS)) {
                    skippedDuplicateCount++;
                } else {
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to save product: {}", product, e);
            }

            processed++;
            if (processed % batchSize == 0) {
                log.info("Saved batch {}/{}", processed, products.size());
            }
        }

        if (processed % batchSize != 0) {
            log.info("Saved batch {}/{}", processed, products.size());
        }

        log.info(
            "finished publishing products. parsed={}, saved={}, duplicates_skipped={}, invalid_skipped={}",
            products.size(),
            savedCount,
            skippedDuplicateCount,
            invalidProductCount
        );
    }

    private boolean hasRequiredFields(ProductDTO product) {
        return product.sku() != null
            && product.title() != null
            && product.imageUrl() != null
            && product.price() != null
            && product.currency() != null;
    }

    private CreateProductCommand toCommand(ProductDTO dto) {
        return new CreateProductCommand(
            dto.sku(),
            dto.title(),
            dto.description(),
            dto.imageUrl(),
            dto.price(),
            dto.currency(),
            dto.volume(),
            dto.createdTime(),
            dto.categories()
        );
    }

    private ProductDTO parseProduct(String str) {
        try {
            ProductDTO productDto = dtoMapper.readValue(str, ProductDTO.class);
            return ProductDTO.builder()
                .sku(productDto.sku())
                .title(productDto.title())
                .description(productDto.description())
                .imageUrl(productDto.imageUrl())
                .price(productDto.price())
                .currency(productDto.currency())
                .categories(productDto.categories())
                .variants(productDto.variants())
                .createdTime(Instant.now())
                .volume(productDto.volume() == null ? BigDecimal.valueOf(100) : productDto.volume())
                .build();
        } catch (Exception e) {
            log.error("Error parsing product: {}", str, e);
            return null;
        }
    }
}
