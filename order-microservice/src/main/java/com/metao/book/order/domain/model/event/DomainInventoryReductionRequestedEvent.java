package com.metao.book.order.domain.model.event;

import com.metao.book.shared.domain.base.DomainEvent;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DomainProductUpdatedEvent extends DomainEvent {

    private final String sku;

    public DomainProductUpdatedEvent(
        @NotNull Instant occurredOn,
        @NotNull String sku
    ) {
        super(occurredOn);
        this.sku = sku;
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    @NotNull
    @Override
    public String getEventType() {
        return "ProductUpdatedEvent";
    }
}
