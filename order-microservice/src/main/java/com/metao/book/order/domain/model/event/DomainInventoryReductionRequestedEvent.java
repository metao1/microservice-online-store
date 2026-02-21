package com.metao.book.order.domain.model.event;

import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DomainInventoryReductionRequestedEvent extends DomainEvent {

    private final ProductSku sku;
    private final Quantity volume;

    public DomainInventoryReductionRequestedEvent(
        @NotNull Instant occurredOn,
        @NotNull ProductSku sku,
        @NotNull Quantity volume
    ) {
        super(occurredOn);
        this.sku = sku;
        this.volume = volume;
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
