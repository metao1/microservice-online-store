package com.metao.book.shared.spring.persistence;

import com.metao.book.shared.domain.product.ProductSku;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductSkuAttributeConverter implements AttributeConverter<ProductSku, String> {

    @Override
    public String convertToDatabaseColumn(ProductSku attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductSku convertToEntityAttribute(String value) {
        return value == null ? null : ProductSku.of(value);
    }
}
