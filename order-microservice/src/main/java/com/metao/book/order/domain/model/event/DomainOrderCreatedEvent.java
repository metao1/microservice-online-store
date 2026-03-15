package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.shared.domain.base.DomainEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class DomainOrderCreatedEvent extends DomainEvent {

    private final OrderId orderId;
    private final UserId userId;

    public DomainOrderCreatedEvent(OrderId orderId, UserId userId) {
        super(Instant.now());
        this.orderId = orderId;
        this.userId = userId;
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    @Override
    public String getEventType() {
        return "DomainOrderCreatedEvent";
    }
}