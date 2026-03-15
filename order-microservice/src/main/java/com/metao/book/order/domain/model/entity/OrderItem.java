package com.metao.book.order.domain.model.entity;

import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import lombok.Getter;

@Getter
public class OrderItem {
    private final ProductSku productSku;
    private final Money unitPrice;
    private final ProductTitle title;
    private Quantity quantity;

    public OrderItem(ProductSku productSku, ProductTitle title, Quantity quantity, Money unitPrice) {
        this.productSku = productSku;
        this.title = title;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void updateQuantity(Quantity newQuantity) {
        this.quantity = newQuantity;
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity.value());
    }
}
