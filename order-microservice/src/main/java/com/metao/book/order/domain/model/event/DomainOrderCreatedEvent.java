package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Getter;

@Getter
public class DomainOrderCreatedEvent extends DomainEvent {

    private final OrderId orderId;
    private final CustomerId customerId;

    public DomainOrderCreatedEvent(OrderId orderId, CustomerId customerId) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
    }

    @Override
    public String getEventType() {
        return "OrderCreated";
    }
}