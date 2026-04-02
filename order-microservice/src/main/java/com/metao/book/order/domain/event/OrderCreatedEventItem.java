package com.metao.book.order.domain.event;

import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.util.Objects;

public record OrderCreatedEventItem(
    ProductSku productSku,
    ProductTitle productTitle,
    Quantity quantity,
    Money unitPrice
) {

    public OrderCreatedEventItem {
        Objects.requireNonNull(productSku, "productSku can't be null");
        Objects.requireNonNull(productTitle, "productTitle can't be null");
        Objects.requireNonNull(quantity, "quantity can't be null");
        Objects.requireNonNull(unitPrice, "unitPrice can't be null");
    }
}
