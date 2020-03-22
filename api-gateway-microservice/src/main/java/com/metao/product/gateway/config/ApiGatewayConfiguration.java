package com.metao.product.gateway.config;

import com.metao.product.gateway.filter.ThrottleGatewayFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableAutoConfiguration
public class ApiGatewayConfiguration {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder rb) {
        return rb.routes()
                .route(r -> r.path("/api/v1/products/*").uri("lb://product-ms")
                        .filter(ThrottleGatewayFilter
                                .builder()
                                .capacity(1)
                                .refillPeriod(10)
                                .refillTokens(1)
                                .refillUnit(TimeUnit.SECONDS)
                                .build()))
                .build();
    }
}
