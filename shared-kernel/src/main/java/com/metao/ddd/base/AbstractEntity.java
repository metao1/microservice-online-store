package com.metao.ddd.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.NonNull;

import javax.persistence.Id;
import java.util.Objects;

public abstract class AbstractEntity<T extends DomainObjectId> implements IdentifiableDomainObject<T> {

    @Id
    @JsonProperty("id")
    private T id;

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
    protected AbstractEntity(@NonNull AbstractEntity<T> source) {
        Objects.requireNonNull(source, "source must not be null");
        this.id = source.id;
    }

    /**
     * Constructor for creating new entities.
     *
     * @param id the ID to assign to the entity.
     */
    protected AbstractEntity(@NonNull T id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    @Override
    @NonNull
    public T id() {
        return id;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass") // We do this with a Spring function
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(ProxyUtils.getUserClass(obj))) {
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
        return String.format("%s[%s]", getClass().getSimpleName(), id);
    }
}
