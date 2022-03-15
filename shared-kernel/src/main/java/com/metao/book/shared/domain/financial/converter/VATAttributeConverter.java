package com.metao.book.shared.domain.financial.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.metao.book.shared.domain.financial.VAT;

/**
 * JPA attribute converter for {@link VAT}.
 */
@Converter(autoApply = true)
public class VATAttributeConverter implements AttributeConverter<VAT, Integer> {

    @Override
    public Integer convertToDatabaseColumn(VAT attribute) {
        return attribute == null ? null : attribute.toInteger();
    }

    @Override
    public VAT convertToEntityAttribute(Integer dbData) {
        return VAT.valueOf(dbData);
    }
}
