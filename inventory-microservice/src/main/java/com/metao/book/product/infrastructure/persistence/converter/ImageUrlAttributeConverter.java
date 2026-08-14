package com.metao.book.product.infrastructure.persistence.converter;

import com.metao.book.product.domain.model.valueobject.ImageUrl;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ImageUrlAttributeConverter implements AttributeConverter<ImageUrl, String> {

    @Override
    public String convertToDatabaseColumn(ImageUrl imageUrl) {
        return imageUrl == null ? null : imageUrl.getValue();
    }

    @Override
    public ImageUrl convertToEntityAttribute(String value) {
        return value == null ? null : ImageUrl.of(value);
    }
}
