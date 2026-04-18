package com.metao.book.order.application.config;

import com.metao.book.shared.domain.financial.VAT;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Environment-driven VAT configuration for the order microservice.
 *
 * <p>Bound to the {@code app.order.vat} prefix. Defaults to a zero-rate so services
 * that do not explicitly configure VAT keep their historical behaviour.</p>
 *
 * @param percentage integer VAT rate where e.g. {@code 21} means 21 %.
 */
@Validated
@ConfigurationProperties(prefix = "app.order.vat")
public record VatProperties(@Min(0) @DefaultValue("0") int percentage) {

    /**
     * Materialise the configured percentage into the shared-kernel value object.
     */
    public VAT toVat() {
        return new VAT(percentage);
    }
}
