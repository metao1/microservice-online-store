package com.metao.product.retails.config;

import com.google.gson.Gson;
import com.metao.product.retails.domain.ProductEntity;
import com.metao.product.retails.mapper.ProductMapper;
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
import java.util.Objects;
import java.util.UUID;
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
    private final ProductMapper productMapper;

    @PostConstruct
    @Async
    public void initDatabase() {
        try {
            final AtomicInteger counter = new AtomicInteger();
            log.debug("importing products data from resources");
            String dataSource = readDataFromResources("data/products.json");
            Stream.of(dataSource.split("!"))
                    .map(s -> {
                                ProductEntity productEntity = null;
                                try {
                                    productEntity = gson.fromJson(s, ProductEntity.class);
                                    if (productEntity != null && counter.get() < 1000 ) {
                                        productEntity.setDescription(productEntity.getDescription() != null ? productEntity.getDescription().length() > 255 ? productEntity.getDescription().substring(0, 255) : productEntity.getDescription() : "The description is not provided for this product. Please consider looking at the other websites in the internet for more information about this product.");
                                        productEntity.setPrice(12d);
                                        productEntity.setId(UUID.randomUUID().toString());
                                        productService.saveProduct(productMapper.toDto(productEntity));
                                    }
                                } catch (Exception ex) {
                                    log.error(ex.getMessage());
                                }
                                if (counter.getAndIncrement() > 1000) {
                                    return null;
                                }
                                return productEntity;
                            }
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        log.debug("coll");
    }

    private String readDataFromResources(String path) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (Stream<String> content = Files.lines(Paths.get(classLoader.getResource(path).getFile()))) {
            return content.collect(Collectors.joining("!"));
        }
    }
}
