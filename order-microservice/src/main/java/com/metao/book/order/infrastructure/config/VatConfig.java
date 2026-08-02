package com.metao.book.order.infrastructure.config;

import com.metao.book.shared.domain.financial.VAT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(VatProperties.class)
public class VatConfig {

    @Bean
    public VAT vat(VatProperties properties) {
        VAT vat = properties.toVat();
        log.info("Order microservice VAT rate configured at {} (from app.order.vat.percentage).", vat);
        return vat;
    }
}
