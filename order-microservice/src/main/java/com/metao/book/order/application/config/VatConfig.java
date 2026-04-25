package com.metao.book.order.application.config;

import com.metao.book.shared.domain.financial.VAT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the configured {@link VAT} value object as a Spring bean so it can be injected
 * wherever order totals are computed or aggregates are hydrated.
 */
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
