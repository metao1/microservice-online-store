package com.metao.book.order.infrastructure.config;

import com.metao.book.shared.domain.financial.VAT;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.order.vat")
public record VatProperties(@Min(0) @DefaultValue("0") int percentage) {

    public VAT toVat() {
        return VAT.valueOf(percentage);
    }
}
