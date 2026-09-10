package com.metao.book.order.domain.event;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record OrderCreatedEvent(
    String eventId,
    OrderId orderId,
    UserId userId,
    List<OrderCreatedEventItem> items,
    OrderStatus status,
    Instant createdAt,
    Instant updatedAt
) {

    /** Compatibility constructor for local callers that do not have a transport envelope. */
    public OrderCreatedEvent(
        OrderId orderId,
        UserId userId,
        List<OrderCreatedEventItem> items,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(orderId.value(), orderId, userId, items, status, createdAt, updatedAt);
    }

    public OrderCreatedEvent {
        Objects.requireNonNull(eventId, "eventId can't be null");
        if (eventId.isBlank()) {
            throw new IllegalArgumentException("eventId can't be blank");
        }
        Objects.requireNonNull(orderId, "orderId can't be null");
        Objects.requireNonNull(userId, "userId can't be null");
        Objects.requireNonNull(items, "items can't be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items can't be empty");
        }
        items = List.copyOf(items);
        Objects.requireNonNull(status, "status can't be null");
        Objects.requireNonNull(createdAt, "createdAt can't be null");
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }
}
