package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.financial.VAT;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class DomainOrderCreatedEvent extends DomainEvent {

    private final OrderId orderId;
    private final UserId userId;
    private final List<OrderItem> items;
    private final Money subtotal;
    private final Money tax;
    private final Money total;
    private final VAT vat;

    public DomainOrderCreatedEvent(
        OrderId orderId,
        UserId userId,
        List<OrderItem> items,
        Money subtotal,
        Money tax,
        Money total,
        VAT vat,
        Instant createdAt
    ) {
        super(createdAt);
        this.orderId = Objects.requireNonNull(orderId, "orderId can't be null");
        this.userId = Objects.requireNonNull(userId, "userId can't be null");
        this.items = List.copyOf(Objects.requireNonNull(items, "items can't be null"));
        this.subtotal = Objects.requireNonNull(subtotal, "subtotal can't be null");
        this.tax = Objects.requireNonNull(tax, "tax can't be null");
        this.total = Objects.requireNonNull(total, "total can't be null");
        this.vat = Objects.requireNonNull(vat, "vat can't be null");
    }

    /**
     * Get the type of this event (for serialization/routing)
     */
    @Override
    public String getEventType() {
        return "DomainOrderCreatedEvent";
    }
}
