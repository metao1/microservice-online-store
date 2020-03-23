package com.metao.product.retails.config;

import com.google.gson.Gson;
import com.metao.product.retails.model.ProductDTO;
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
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@Profile("setup")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataStoreConfig {

    private final Gson gson;
    private final ProductService productService;

    @PostConstruct
    @Async
    public void initDatabase() {
        try {
            log.debug("importing products data from resources");
            String dataSource = readDataFromResources("data/products.json");
            Stream.of(dataSource.split("!"))
                    .map(s -> {
                                ProductDTO productDTO = null;
                                try {
                                    productDTO = gson.fromJson(s, ProductDTO.class);
                                    if (productDTO != null) {
                                        productDTO.setBrand("Some brand");
                                        productDTO.setDescription(productDTO.getDescription() != null ? productDTO.getDescription().length() > 255 ? productDTO.getDescription().substring(0, 255) : productDTO.getDescription() : "The description is not provided for this product. Please consider looking at the other websites in the internet for more information about this product.");
                                        productDTO.setCreatedAt(new Date());
                                        productDTO.setModifiedAt(new Date());
                                        productDTO.setCreatedBy("Mehrdad");
                                        productDTO.setModifiedBy("Mehrdad");
                                        productDTO.setPrice(12d);
                                        productDTO.setAsin(UUID.randomUUID().toString());
                                        productService.saveProduct(productDTO);
                                    }
                                } catch (Exception ex) {
                                    log.error(ex.getMessage());
                                }
                                return productDTO;
                            }
                    ).collect(Collectors.toList());
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
