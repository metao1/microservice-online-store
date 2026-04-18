package com.metao.book.payment.config;

import com.metao.book.shared.config.OpenApiConfigFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private int serverPort;

    @Bean
    public OpenAPI paymentOpenAPI() {
        return OpenApiConfigFactory.create(
            "Payment Service API",
            "Payment processing service",
            serverPort
        );
    }
}
