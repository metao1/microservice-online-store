package com.metao.book.payment.application.config;

import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.payment.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentDomainConfiguration {

    @Bean
    PaymentDomainService paymentDomainService(PaymentRepository paymentRepository) {
        return new PaymentDomainService(paymentRepository);
    }
}
