package com.metao.book.order.domain.model.valueobject;

import com.metao.book.shared.architecture.DomainComponent;
import java.util.List;

@DomainComponent
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
            case CREATED -> List.of(PENDING_PAYMENT, PAID, PAYMENT_FAILED, CANCELLED).contains(target);
            case PENDING_PAYMENT -> List.of(PAID, PAYMENT_FAILED, CANCELLED).contains(target);
            case PAYMENT_FAILED -> List.of(PENDING_PAYMENT, CANCELLED).contains(target);
            case PAID -> List.of(PROCESSING, CANCELLED).contains(target);
            case PROCESSING -> List.of(SHIPPED, CANCELLED).contains(target);
            case SHIPPED -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
