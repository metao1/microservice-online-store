package com.metao.book.product.config;

import com.metao.book.shared.config.OpenApiConfigFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8083}")
    private int serverPort;

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return OpenApiConfigFactory.create(
            "Inventory Service API",
            "Product catalog and inventory management service",
            serverPort
        );
    }
}
