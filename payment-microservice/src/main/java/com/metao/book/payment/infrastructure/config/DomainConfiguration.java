package com.metao.book.payment.infrastructure.config;

import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.payment.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain services and components
 */
@Configuration
public class DomainConfiguration {

    @Bean
    public PaymentDomainService paymentDomainService(PaymentRepository paymentRepository) {
        return new PaymentDomainService(paymentRepository);
    }
}
