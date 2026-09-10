package com.metao.book.order.domain.model.event;

import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.order.domain.model.valueobject.OrderId;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DomainInventoryReductionRequestedEvent extends DomainEvent {

    private final ProductSku sku;
    private final Quantity volume;
    private final OrderId orderId;

    public DomainInventoryReductionRequestedEvent(
        @NotNull Instant occurredOn,
        @NotNull OrderId orderId,
        @NotNull ProductSku sku,
        @NotNull Quantity volume
    ) {
        super(occurredOn);
        this.orderId = orderId;
        this.sku = sku;
        this.volume = volume;
    }

    public DomainInventoryReductionRequestedEvent(
        @NotNull Instant occurredOn,
        @NotNull ProductSku sku,
        @NotNull Quantity volume
    ) {
        this(occurredOn, new OrderId("unknown"), sku, volume);
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    @Override
    public String getEventType() {
        return "InventoryReductionRequestedEvent";
    }
}
