package com.metao.ddd.shared.infra.hibernate;

import com.metao.ddd.shared.domain.base.DomainObjectId;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Hibernate type descriptor for a {@link DomainObjectId} subtype. You typically
 * don't need to subclass this.
 *
 * @param <T> the T type.
 * @see DomainObjectIdCustomType
 */
public class DomainObjectIdTypeDescriptor<T extends DomainObjectId> extends AbstractTypeDescriptor<T> {

    private final transient Function<String, T> factory;

    /**
     * Creates a new {@code DomainObjectIdTypeDescriptor}.
     *
     * @param type    the ID type.
     * @param factory a factory for creating new ID instances.
     */
    public DomainObjectIdTypeDescriptor(@NonNull Class<T> type, @NonNull Function<String, T> factory) {
        super(type);
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
    }

    @Override
    public String toString(T value) {
        return value.toUUID();
    }

    @Override
    public T fromString(String string) {
        return factory.apply(string);
    }

    @Override
    public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (type.isAssignableFrom(getJavaType())) {
            return type.cast(value);
        }
        if (type.isAssignableFrom(String.class)) {
            return type.cast(toString(value));
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> T wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (getJavaType().isInstance(value)) {
            return getJavaType().cast(value);
        }
        if (value instanceof String) {
            return fromString((String) value);
        }
        throw unknownWrap(value.getClass());
    }
}
