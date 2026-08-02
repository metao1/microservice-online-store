package com.metao.book.shared.domain.base;

import java.util.Objects;

/**
 * Base class for entities.
 *
 * @param <T> the entity T type.
 */
public abstract class AbstractEntity<T extends ValueObject> implements IdentifiableDomainObject<T> {

    protected T id;

    /**
     * Default constructor
     */
    protected AbstractEntity() {
    }

    /**
     * Copy constructor
     *
     * @param source the entity to copy from.
     */
    protected AbstractEntity(AbstractEntity<T> source) {
        this.id = Objects.requireNonNull(source.id, "source.id must not be null");
    }

    /**
     * Constructor for creating new entities.
     *
     * @param id the ID to assign to the entity.
     */
    protected AbstractEntity(T id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    @Override
    public T id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }

        var other = (AbstractEntity<?>) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }

    @Override
    public String toString() {
        return "AbstractEntity{" +
            "id=" + id +
            '}';
    }
}
