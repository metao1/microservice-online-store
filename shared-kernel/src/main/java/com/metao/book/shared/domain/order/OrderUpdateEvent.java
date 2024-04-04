package com.metao.book.shared.domain.order;

import com.metao.book.shared.domain.base.DomainEvent;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;

@Builder
public record OrderUpdateEvent(String id, OrderEvent event, Instant occurredOn) implements Serializable,
    DomainEvent {
}
