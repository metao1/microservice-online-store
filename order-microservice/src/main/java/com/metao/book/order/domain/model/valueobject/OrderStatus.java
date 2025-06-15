package com.metao.book.order.domain.model.valueobject;

public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}