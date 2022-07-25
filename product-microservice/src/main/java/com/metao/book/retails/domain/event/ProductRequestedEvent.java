package com.metao.book.retails.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metao.book.retails.domain.ProductId;
import com.metao.book.shared.domain.base.DomainEvent;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Objects;

@Builder(toBuilder = true)
public class ProductRequestedEvent implements DomainEvent {

    @JsonProperty("productId")
    private final ProductId productId;
    @JsonProperty("occurredOn")
    private final Instant occurredOn;

    @JsonCreator
    private ProductRequestedEvent(@NonNull ProductId productId,
                                 Instant occurredOn) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(occurredOn, "occurredOn must not be null");
        this.productId = productId;
        this.occurredOn = occurredOn;
    }

    @NonNull
    public ProductId productId() {
        return productId;
    }

    @Override
    @NonNull
    public Instant occurredOn() {
        return occurredOn;
    }

}
