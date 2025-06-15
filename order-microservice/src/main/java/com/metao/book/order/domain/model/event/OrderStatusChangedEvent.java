package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Getter;

@Getter
public class OrderStatusChangedEvent extends DomainEvent {

    private final OrderId orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(OrderId orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        super();
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "OrderStatusChanged";
    }
}