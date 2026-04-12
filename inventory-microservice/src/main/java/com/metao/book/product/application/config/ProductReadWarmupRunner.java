package com.metao.book.product.application.config;

import com.metao.book.product.application.mapper.ProductApplicationMapper;
import com.metao.book.product.application.service.ProductDomainService;
import com.metao.book.product.domain.model.valueobject.CategoryName;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "product.prewarm.enabled", havingValue = "true")
public class ProductReadWarmupRunner implements ApplicationRunner {

    private final ProductDomainService productDomainService;
    private final ProductApplicationMapper productApplicationMapper;

    @Value("${product.prewarm.categories:books}")
    private String prewarmCategories;

    @Value("${product.prewarm.offset:0}")
    private int offset;

    @Value("${product.prewarm.limit:16}")
    private int limit;

    @Override
    public void run(ApplicationArguments args) {
        for (String category : parseCategories()) {
            prewarmCategory(category);
        }
    }

    private List<String> parseCategories() {
        return Arrays.stream(prewarmCategories.split(","))
            .map(String::trim)
            .filter(category -> !category.isBlank())
            .distinct()
            .toList();
    }

    private void prewarmCategory(String category) {
        long startedAt = System.nanoTime();
        try {
            var products = productDomainService.getProductsByCategory(CategoryName.of(category), offset, limit);
            products.stream()
                .map(productApplicationMapper::toDTO)
                .toList();

            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info(
                "Prewarmed product category page: category={}, offset={}, limit={}, resultCount={}, elapsedMs={}",
                category,
                offset,
                limit,
                products.size(),
                elapsedMs
            );
        } catch (Exception ex) {
            log.warn(
                "Failed to prewarm product category page: category={}, offset={}, limit={}",
                category,
                offset,
                limit,
                ex
            );
        }
    }
}
