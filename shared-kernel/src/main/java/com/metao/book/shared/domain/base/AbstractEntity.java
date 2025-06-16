package com.metao.book.shared.domain.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import org.springframework.lang.NonNull;

/**
 * Base class for entities.
 *
 * @param <T> the entity T type.
 */
@MappedSuperclass
public abstract class AbstractEntity<T extends ValueObject> implements IdentifiableDomainObject<T> {

    @Id
    @JsonProperty("id")
    @Column(name = "id", unique = true, nullable = false)
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
