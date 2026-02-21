package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;

@Getter
public class OrderItemAddedEvent extends DomainEvent {

    private final OrderId orderId;
    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;

    public OrderItemAddedEvent(
        OrderId orderId,
        ProductId productId,
        Quantity quantity,
        Money unitPrice
    ) {
        super(Instant.now());
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @NotNull
    @Override
    public String getEventType() {
        return "OrderItemAdded";
    }
}