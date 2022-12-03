package com.metao.book.shared.domain.base;

import java.time.Instant;

/**
 * Interface for domain events.
 */
public interface DomainEvent extends DomainObject {

    String id();

    Instant occurredOn();
}
