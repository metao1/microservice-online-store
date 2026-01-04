package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.shared.domain.base.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class DomainOrderCreatedEvent extends DomainEvent {

    private final OrderId orderId;
    private final CustomerId customerId;

    public DomainOrderCreatedEvent(OrderId orderId, CustomerId customerId) {
        super(Instant.now());
        this.orderId = orderId;
        this.customerId = customerId;
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    @Override
    public String getEventType() {
        return "DomainOrderCreatedEvent";
    }
}