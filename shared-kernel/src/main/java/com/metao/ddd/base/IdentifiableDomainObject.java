package com.metao.ddd.base;

import java.io.Serializable;

/**
 * Interface for domain objects that can be uniquely identified.
 *
 * @param <T> the T type.
 */
public interface IdentifiableDomainObject<T extends Serializable> extends DomainObject {

    /**
     * Returns the T of this domain object.
     *
     * @return the T or {@code null} if an T has not been assigned yet.
     */
    T id();
}
