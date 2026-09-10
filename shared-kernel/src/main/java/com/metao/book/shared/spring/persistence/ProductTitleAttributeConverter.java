package com.metao.book.shared.spring.persistence;

import com.metao.book.shared.domain.product.ProductTitle;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductTitleAttributeConverter implements AttributeConverter<ProductTitle, String> {

    @Override
    public String convertToDatabaseColumn(ProductTitle attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductTitle convertToEntityAttribute(String value) {
        return value == null ? null : ProductTitle.of(value);
    }
}
