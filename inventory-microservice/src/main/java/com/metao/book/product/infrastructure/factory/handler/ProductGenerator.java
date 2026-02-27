package com.metao.book.product.infrastructure.factory.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metao.book.product.application.dto.ProductDTO;
import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.domain.model.aggregate.ProductAggregate;
import com.metao.book.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "generator")
public class ProductGenerator {

    @Value("classpath:data/products.txt")
    Resource resource;

    private final ObjectMapper dtoMapper;
     private final ProductRepository productRepository;
    private final EntityManager entityManager;

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
        final List<ProductAggregate> products;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            products = reader.lines()
                .map(this::parseProduct)
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException e) {
            log.error("Error reading products file", e);
            return;
        }

        log.info("Parsed {} products, starting batch save", products.size());

        // Save in batches of 50
        int batchSize = 50;
        int savedCount = 0;
        int skippedDuplicateCount = 0;
        int processed = 0;
        for (ProductAggregate product : products) {
            try {
                boolean inserted = productRepository.insertIfAbsent(product);
                if (!inserted) {
                    skippedDuplicateCount++;
                    log.warn("Skipping duplicate product SKU: {}", product.getId());
                } else {
                    savedCount++;
                    log.debug("Saved product: {}", product.getId());
                }
            } catch (Exception e) {
                log.error("Failed to save product: {}", product.getId(), e);
            }

            processed++;
            if (processed % batchSize == 0) {
                // Flush and clear session every batch
                productRepository.flush();
                entityManager.clear();
                log.info("Saved batch {}/{}", processed, products.size());
            }
        }

        if (processed % batchSize != 0) {
            productRepository.flush();
            entityManager.clear();
            log.info("Saved batch {}/{}", processed, products.size());
        }

        log.info(
            "finished publishing products. parsed={}, saved={}, duplicates_skipped={}",
            products.size(),
            savedCount,
            skippedDuplicateCount
        );
    }

    private ProductAggregate parseProduct(String str) {
        try {
            ProductDTO productDto = dtoMapper.readValue(str, ProductDTO.class);
            productDto = ProductDTO.builder()
                .sku(productDto.sku())
                .title(productDto.title())
                .description(productDto.description())
                .imageUrl(productDto.imageUrl())
                .price(productDto.price())
                .currency(productDto.currency())
                .categories(productDto.categories())
                .volume(BigDecimal.ZERO)
                .build();
            return ProductApplicationMapper.toDomain(productDto);
        } catch (Exception e) {
            log.error("Error parsing product: {}", str, e);
            return null;
        }
    }
}
