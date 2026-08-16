package com.metao.book.product.infrastructure.persistence.converter;

import com.metao.book.product.domain.model.valueobject.ProductDescription;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductDescriptionAttributeConverter
    implements AttributeConverter<ProductDescription, String> {

    @Override
    public String convertToDatabaseColumn(ProductDescription description) {
        return description == null ? null : description.value();
    }

    @Override
    public ProductDescription convertToEntityAttribute(String value) {
        return value == null ? null : ProductDescription.of(value);
    }
}
