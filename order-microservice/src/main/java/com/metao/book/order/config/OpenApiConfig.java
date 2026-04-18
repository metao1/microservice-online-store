package com.metao.book.order.config;

import com.metao.book.shared.config.OpenApiConfigFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8086}")
    private int serverPort;

    @Bean
    public OpenAPI orderOpenAPI() {
        return OpenApiConfigFactory.create(
            "Order Service API",
            "Shopping cart and order management service",
            serverPort
        );
    }
}
