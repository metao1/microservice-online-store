package com.metao.book.order.domain.model.valueobject;

import java.util.Set;

public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> Set.of(PENDING_PAYMENT, PAID, PAYMENT_FAILED, CANCELLED).contains(target);
            case PENDING_PAYMENT -> Set.of(PAID, PAYMENT_FAILED, CANCELLED).contains(target);
            case PAYMENT_FAILED -> Set.of(PENDING_PAYMENT, CANCELLED).contains(target);
            case PAID -> Set.of(PROCESSING, CANCELLED).contains(target);
            case PROCESSING -> Set.of(SHIPPED, CANCELLED).contains(target);
            case SHIPPED -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
