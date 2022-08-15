package com.metao.book.shared.domain.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.NonNull;

import java.util.*;

/**
 * Base class for aggregate roots.
 *
 * @param <T> the aggregate root T type.
 */
public abstract class AbstractAggregateRoot<T extends DomainObjectId> extends AbstractEntity<T> {

    @JsonIgnore
    private List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Default constructor
     */
    protected AbstractAggregateRoot() {
    }

    /**
     * Copy constructor. Please note that any registered domain events are
     * <b>not</b> copied.
     *
     * @param source the aggregate root to copy from.
     */
    protected AbstractAggregateRoot(@NonNull AbstractAggregateRoot<T> source) {
        super(source);
    }

    /**
     * Constructor for creating new aggregate roots.
     *
     * @param id the ID to assign to the aggregate root.
     */
    protected AbstractAggregateRoot(T id) {
        super(id);
    }

    /**
     * Registers the given domain event to be published when the aggregate root is
     * persisted.
     *
     * @param event the event to register.
     */
    @NonNull
    protected void registerEvent(@NonNull DomainEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        this.domainEvents.add(event);
    }

    /**
     * Called by the persistence framework to clear all registered domain events
     * once they have been published.
     */
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * Returns all domain events that have been registered for publication. Intended
     * to be used by the persistence
     * framework only.
     */
    protected Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
