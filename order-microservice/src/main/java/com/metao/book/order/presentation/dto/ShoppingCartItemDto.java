package com.metao.book.order.presentation.dto;

import com.metao.book.order.application.cart.ShoppingCartItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

public record ShoppingCartItemDto(
    @NotBlank String sku,
    @NotBlank String productTitle,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
    @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
    @NotNull Currency currency
) {

    public ShoppingCartItem toApplicationItem() {
        return new ShoppingCartItem(sku, productTitle, quantity, price, currency);
    }

    public static ShoppingCartItemDto from(ShoppingCartItem item) {
        return new ShoppingCartItemDto(
            item.sku(), item.productTitle(), item.quantity(), item.price(), item.currency());
    }
}
