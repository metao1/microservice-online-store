package com.metao.book.cart.domain.event;

import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Value;

import java.time.Instant;

@Value
public class OrderAddedToCartEvent implements DomainEvent {
    String cartKey;
    String userId;
    String asin;
    int quantity;
    Instant occurredOn;

    @Override
    public String id() {
        return asin;
    }

    public Instant occurredOn() {
        return occurredOn;
    }
}
