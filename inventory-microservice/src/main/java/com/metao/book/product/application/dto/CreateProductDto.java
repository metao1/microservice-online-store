package com.metao.book.product.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Valid
public record CreateProductDto(
    @NotBlank(message = "SKU cannot be blank")
    String sku,
    @NotBlank(message = "Title cannot be blank")
    String title,
    @NotBlank(message = "Image URL cannot be blank")
    String description,
    String imageUrl,
    @NotBlank(message = "Price cannot be blank")
    @DecimalMin(value = "0.00", message = "Price must be greater than or equal to zero")
    BigDecimal price,
    @NotBlank(message = "Currency cannot be blank")
    Currency currency,
    @NotBlank(message = "Volume cannot be blank")
    BigDecimal volume,
    @NotBlank(message = "Categories cannot be blank")
    List<String> categories
) {
}
