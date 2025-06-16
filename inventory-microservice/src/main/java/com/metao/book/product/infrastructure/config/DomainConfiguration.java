package com.metao.book.product.infrastructure.config;

import com.metao.book.product.domain.repository.CategoryRepository;
import com.metao.book.product.domain.repository.ProductRepository;
import com.metao.book.product.domain.service.ProductDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain services and components
 */
@Configuration
public class DomainConfiguration {

    @Bean
    public ProductDomainService productDomainService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository
    ) {
        return new ProductDomainService(productRepository, categoryRepository);
    }
}
