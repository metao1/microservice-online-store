package com.metao.book.shared.domain.base;

import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Base class for domain events in DDD
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredOn;

    protected DomainEvent(Instant occurredOn) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = occurredOn;
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    public abstract String getEventType();
}
