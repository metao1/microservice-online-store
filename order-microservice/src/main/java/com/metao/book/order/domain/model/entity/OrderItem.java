package com.metao.book.order.domain.model.entity;

import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.shared.domain.financial.Money;
import lombok.Getter;

@Getter
public class OrderItem {

    private final ProductId productId;
    private final Money unitPrice;
    private Quantity quantity;

    public OrderItem(ProductId productId, Quantity quantity, Money unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void updateQuantity(Quantity newQuantity) {
        this.quantity = newQuantity;
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity.getValue());
    }
}