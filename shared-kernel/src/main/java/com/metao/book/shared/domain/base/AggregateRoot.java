package com.metao.book.shared.domain.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

/**
 * Base class for aggregate roots in DDD
 */
@Getter
public abstract class AggregateRoot<D> extends Entity<D> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot() {
        super();
    }

    protected AggregateRoot(D id) {
        super(id);
    }

    /**
     * Add a domain event to be published
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Get all domain events (read-only)
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clear all domain events (typically called after publishing)
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * Check if there are any unpublished domain events
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
}
