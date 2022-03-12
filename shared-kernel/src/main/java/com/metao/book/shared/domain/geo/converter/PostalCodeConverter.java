package com.metao.book.shared.domain.geo.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.metao.book.shared.domain.geo.PostalCode;

/**
 * JPA attribute converter for {@link PostalCode}.
 */
@Converter(autoApply = true)
public class PostalCodeConverter implements AttributeConverter<PostalCode, String> {

    @Override
    public String convertToDatabaseColumn(PostalCode attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public PostalCode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new PostalCode(dbData);
    }
}
