package com.metao.book.order.domain.model.entity;

import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import lombok.Getter;

@Getter
public class OrderItem {
    private final ProductSku productSku;
    private final Money unitPrice;
    private Quantity quantity;

    public OrderItem(ProductSku productSku, Quantity quantity, Money unitPrice) {
        this.productSku = productSku;
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
