package com.metao.book.product.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.hibernate.validator.constraints.Length;

/**
 * Command for creating a new product
 */
public record CreateProductCommand(
    @NotNull
    @Pattern(regexp = "^\\w{10}$", message = "SKU must be exactly 10 characters")
    String sku,

    @NotNull
    @Length(min = 3, max = 2500, message = "Title must be between 3 and 2500 characters")
    String title,

    @Length(max = 10_485_760, message = "Description cannot exceed 10MB")
    String description,

    @NotNull
    @Pattern(regexp = "(http(s?):)([/|.\\w])*\\.(?:jpg|gif|png)", message = "Invalid image URL format")
    String imageUrl,

    @NotNull
    @Positive(message = "Price must be positive")
    BigDecimal price,

    @NotNull
    Currency currency,

    @NotNull
    @Positive(message = "Volume must be positive")
    BigDecimal volume,

    List<String> categoryNames
) {
}
