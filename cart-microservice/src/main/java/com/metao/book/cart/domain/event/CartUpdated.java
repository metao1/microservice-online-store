package com.metao.book.cart.domain.event;

import com.metao.book.shared.domain.base.DomainEvent;
import com.sun.istack.NotNull;
import lombok.Value;

import java.time.Instant;

@Value
public class CartUpdated implements DomainEvent {
    String cartKey;
    String userId;
    String asin;
    int quantity;
    Instant occurredOn;

    @Override
    @NotNull
    public Instant occurredOn() {
        return occurredOn;
    }
}
