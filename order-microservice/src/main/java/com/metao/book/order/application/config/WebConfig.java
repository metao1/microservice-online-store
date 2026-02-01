package com.metao.book.order.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Allow CORS for all paths
            .allowedOrigins("http://localhost:3000")  // Allow specific origin (e.g., React app on port 3000)
            .allowedMethods("GET", "POST", "PUT", "DELETE")  // Allowed HTTP methods
            .allowedHeaders("*")  // Allow any headers
            .allowCredentials(true);  // Allow credentials (e.g., cookies)
    }
}