package com.metao.product.retails.config;

import com.google.gson.Gson;
import com.metao.product.models.ProductDTO;
import com.metao.product.retails.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@Profile("setup")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataStoreConfig {

    private final Gson gson;
    private final ProductService productService;
    final AtomicInteger counter = new AtomicInteger();

    @PostConstruct
    @Async
    public void initDatabase() {
        log.debug("importing products data from resources");
        try {
            String dataSource = readDataFromResources();
            Stream.of(dataSource.split("!"))
                    .map(this::processAndStoreIntoDatabase)
                    .collect(Collectors.toList());
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
            if (productDTO != null && counter.get() < 2) {
                productService.saveProduct(ProductDTO.builder().asin(productDTO.getAsin())
                        .categories(productDTO.getCategories())
                        .title(productDTO.getTitle())
                        .imageUrl(productDTO.getImageUrl())
                        .description(productDTO.getDescription() != null ?
                                productDTO.getDescription() : defaultString)
                        .price(productDTO.getPrice() > 0 ? productDTO.getPrice() : 10.23)
                        .build());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return true;
    }

    private String readDataFromResources() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (Stream<String> content = Files.lines(Paths.get(classLoader.getResource("data/products.json").getFile()))) {
            return content.collect(Collectors.joining("!"));
        }
    }
}
