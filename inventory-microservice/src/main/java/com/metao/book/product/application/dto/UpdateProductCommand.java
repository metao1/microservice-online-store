package com.metao.book.product.application.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Currency;
import org.hibernate.validator.constraints.Length;

/**
 * Command for updating an existing product
 */
public record UpdateProductCommand(
    @Pattern(regexp = "^\\w{10}$", message = "SKU must be exactly 10 characters")
    String sku,

    @Length(min = 3, max = 2500, message = "Title must be between 3 and 2500 characters")
    String title,

    @Length(max = 10_485_760, message = "Description cannot exceed 10MB")
    String description,

    @Positive(message = "Price must be positive")
    BigDecimal price,

    Currency currency
) {
}
