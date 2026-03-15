package com.metao.book.shared.domain.base;

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for entities in DDD
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Entity<D> {

    @EqualsAndHashCode.Include
    @Setter
    private D id;

    protected Entity() {
    }

    protected Entity(D id) {
        Objects.requireNonNull(id, "id can't be null");
        this.id = id;
    }

}
