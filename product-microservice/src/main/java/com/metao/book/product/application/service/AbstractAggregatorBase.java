package com.metao.book.product.application.service;

import com.metao.book.product.application.model.MetadataAware;
import com.metao.book.product.application.model.UserAware;
import java.time.Instant;
import java.util.Objects;
import org.apache.kafka.streams.kstream.Aggregator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public abstract class AbstractAggregatorBase
    <R extends MetadataAware & UserAware, V extends MetadataAware & UserAware>
    implements Aggregator<String, V, R>, MetadataHandler, UserHandler {

    /**
     * Compute a new aggregate from the key and value of a record and the current aggregate of the same key.
     *
     * @param key       the key of the record
     * @param value     the value of the record
     * @param aggregate the current aggregate value
     * @return the new aggregate value
     */
    @Override
    public R apply(@NonNull String key, @Nullable V value, @Nullable R aggregate) {
        Objects.requireNonNull(aggregate, "Requires non-null aggregate");
        if (Objects.isNull(value)) {
            return aggregate;
        }
        var result = process(key, value, aggregate);
        var userOptional = handleUser(value, aggregate);
        var user = userOptional.orElseThrow(() -> new IllegalArgumentException("user was not presented"));
        result.setUser(user);
        var metadata = handleMetadata(key, Instant.now());
        aggregate.setMetadata(metadata);
        return result;
    }

    protected abstract R process(String key, V value, R aggregate);
}
