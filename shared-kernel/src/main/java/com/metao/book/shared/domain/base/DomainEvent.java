package com.metao.book.shared.domain.base;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Base class for domain events in DDD
 */
@Getter
@EqualsAndHashCode
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }

    protected DomainEvent(LocalDateTime occurredOn) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = occurredOn;
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    public abstract String getEventType();
}
