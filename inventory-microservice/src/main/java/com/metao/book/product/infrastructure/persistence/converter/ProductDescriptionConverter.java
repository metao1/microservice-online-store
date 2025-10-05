package com.metao.book.product.infrastructure.persistence.converter;

import com.metao.book.product.domain.model.valueobject.ProductDescription;
import jakarta.persistence.Converter;
import javax.persistence.AttributeConverter;

@Converter(autoApply = true)
public class ProductDescriptionConverter implements AttributeConverter<ProductDescription, String> {

    /**
     * Converts the value stored in the entity attribute into the data representation to be stored in the database.
     *
     * @param attribute the entity attribute value to be converted
     * @return the converted data to be stored in the database column for the corresponding entity attribute
     */
    @Override
    public String convertToDatabaseColumn(ProductDescription attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * Converts the data stored in the database column into the value to be stored in the entity attribute. Note that it
     * is the responsibility of the converter writer to specify the correct <code>dbData</code> type for the
     * corresponding column for use by the JDBC driver: i.e., persistence providers are not expected to do such type
     * conversion.
     *
     * @param dbData the data from the database column to be converted
     * @return the converted value to be stored in the entity attribute
     */
    @Override
    public ProductDescription convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProductDescription.of(dbData);
    }
}
