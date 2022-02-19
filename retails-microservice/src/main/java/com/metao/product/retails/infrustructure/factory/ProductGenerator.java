package com.metao.product.retails.infrustructure.factory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.metao.product.retails.application.dto.ProductDTO;
import com.metao.product.retails.domain.product.event.CreateProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductGenerator {
    private final Gson gson;
    private final ProductCreator productCreator;

    @Async
    @PostConstruct
    public void initDatabase() {
        log.debug("importing products data from resources");
        try {
            String dataSource = readDataFromResources();
            Stream.of(dataSource.split("!"))
                    .map(this::processAndStoreIntoDatabase)
                    .forEach(success -> {
                        if (success) {
                            log.info("all items have been successfully processed.");
                        } else {
                            log.error("There was an error processing all items.");
                        }
                    });
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        log.debug("finished writing to database.");
    }

    private boolean processAndStoreIntoDatabase(String s) {
        String defaultString = "The description is not provided for this product. " +
                "Please consider looking at the other websites in the internet for " +
                "more information about this product.";

        final ProductDTO productDTO;
        try {
            productDTO = gson.fromJson(s, ProductDTO.class);
            if (productDTO != null) {
                var product = ProductDTO.builder().asin(productDTO.getAsin())
                        .categories(productDTO.getCategories())
                        .title(productDTO.getTitle())
                        .imageUrl(productDTO.getImageUrl())
                        .description(productDTO.getDescription() != null ?
                                productDTO.getDescription() : defaultString)
                        .price(productDTO.getPrice() > 0 ? productDTO.getPrice() : 10.23)
                        .categories(productDTO.getCategories())
                        .build();
                var event = new CreateProductEvent(product, Instant.now(), Instant.now());
                productCreator.onCreateProductEvent(event);
                return true;
            }
        } catch (JsonSyntaxException ex) {
            log.error(ex.getMessage());
            return false;
        }
        return false;
    }

    private String readDataFromResources() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (Stream<String> content = Files.lines(Paths.get(classLoader.getResource("data/products.json").getFile()))) {
            return content.collect(Collectors.joining("!"));
        }
    }
}
