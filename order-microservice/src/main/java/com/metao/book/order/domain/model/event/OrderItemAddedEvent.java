package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import lombok.Getter;

@Getter
public class OrderItemAddedEvent extends DomainEvent {

    private final OrderId orderId;
    private final ProductId productId;
    private final String productName;
    private final Quantity quantity;
    private final Money unitPrice;

    public OrderItemAddedEvent(
        OrderId orderId, ProductId productId, String productName, Quantity quantity,
        Money unitPrice
    ) {
        super();
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @Override
    public String getEventType() {
        return "OrderItemAdded";
    }
}