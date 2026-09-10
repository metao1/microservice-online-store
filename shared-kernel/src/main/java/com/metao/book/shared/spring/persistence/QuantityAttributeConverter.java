package com.metao.book.shared.spring.persistence;

import com.metao.book.shared.domain.product.Quantity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

@Converter
public class QuantityAttributeConverter implements AttributeConverter<Quantity, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Quantity attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Quantity convertToEntityAttribute(BigDecimal value) {
        return value == null ? null : Quantity.of(value);
    }
}
