package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class DomainOrderCreatedEvent extends DomainEvent {

    private final OrderId orderId;
    private final UserId userId;
    private final List<OrderItem> items;
    private final Money total;

    public DomainOrderCreatedEvent(
        OrderId orderId,
        UserId userId,
        List<OrderItem> items,
        Money total,
        Instant createdAt
    ) {
        super(createdAt);
        this.orderId = Objects.requireNonNull(orderId, "orderId can't be null");
        this.userId = Objects.requireNonNull(userId, "userId can't be null");
        this.items = List.copyOf(Objects.requireNonNull(items, "items can't be null"));
        this.total = Objects.requireNonNull(total, "total can't be null");
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    @Override
    public String getEventType() {
        return "DomainOrderCreatedEvent";
    }
}
