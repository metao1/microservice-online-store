package com.metao.book.retails.domain.event;

import com.metao.book.shared.domain.base.DomainEvent;
import org.springframework.lang.NonNull;

import java.time.Instant;

public class GetProductEvent implements DomainEvent {
    private final String productId;
    private final Instant createdOn;
    private final Instant occurredOn;

    public GetProductEvent(
            @NonNull final String productId,
            @NonNull final Instant createdOn,
            @NonNull final Instant occurredOn) {
        this.occurredOn = occurredOn;
        this.createdOn = createdOn;
        this.productId = productId;
    }

    @NonNull
    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public Instant createdOn() {
        return createdOn;
    }

    public String productId() {
        return productId;
    }
}
