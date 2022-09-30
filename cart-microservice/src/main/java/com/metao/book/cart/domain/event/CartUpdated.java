package com.metao.book.cart.domain.event;

import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Value;

import java.time.Instant;

@Value
public class CartUpdated implements DomainEvent {
    String id;
    String userId;
    String asin;
    int quantity;
    Instant occurredOn;
}
