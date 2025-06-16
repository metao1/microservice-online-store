package com.metao.book.order.domain.model.entity;

import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.shared.domain.financial.Money;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class OrderItem {

    private final ProductId productId;
    private final String productName;
    private final Money unitPrice;
    private Quantity quantity;

    public OrderItem(ProductId productId, String productName, Quantity quantity, Money unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void updateQuantity(Quantity newQuantity) {
        this.quantity = newQuantity;
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity.getValue()));
    }
}