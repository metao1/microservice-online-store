package com.metao.book.product.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Valid
public record CreateProductDto(
    @NotBlank(message = "SKU cannot be blank")
    @Pattern(regexp = "^\\w{10}$", message = "SKU must be exactly 10 letters")
    String sku,
    @NotBlank(message = "Title cannot be blank")
    String title,
    @NotBlank(message = "Image URL cannot be blank")
    String description,
    @Pattern(regexp = "(http(s?):)([/|.\\w])*\\.(?:jpg|gif|png)", message = "Invalid image URL format")
    String imageUrl,
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.00", message = "Price must be greater than or equal to zero")
    BigDecimal price,
    @NotNull(message = "Currency cannot be null")
    Currency currency,
    @NotNull(message = "Volume cannot be null")
    @DecimalMin(value = "0.00", message = "Volume must be greater than or equal to zero")
    BigDecimal volume,
    @NotNull(message = "Categories cannot be null")
    List<String> categories
) {
}
