package com.metao.book.order.infrastructure.persistence.converter;

import com.metao.book.order.domain.model.valueobject.UserId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserIdAttributeConverter implements AttributeConverter<UserId, String> {

    @Override
    public String convertToDatabaseColumn(UserId userId) {
        return userId == null ? null : userId.value();
    }

    @Override
    public UserId convertToEntityAttribute(String value) {
        return value == null ? null : UserId.of(value);
    }
}
