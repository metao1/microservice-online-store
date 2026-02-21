package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.shared.domain.base.DomainEvent;
import java.time.Instant;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;

@Getter
public class OrderStatusChangedEvent extends DomainEvent {

    private final OrderId orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(OrderId orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        super(Instant.now());
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @NotNull
    @Override
    public String getEventType() {
        return "OrderStatusChanged";
    }
}