package com.metao.book.order.domain.model.event;

import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.shared.domain.product.Quantity;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;

@Getter
public class DomainOrderItemAddedEvent extends DomainEvent {

    private final OrderId orderId;
    private final ProductSku productSku;
    private final Quantity quantity;
    private final Money unitPrice;

    public DomainOrderItemAddedEvent(
        OrderId orderId,
        ProductSku productSku,
        Quantity quantity,
        Money unitPrice
    ) {
        super(Instant.now());
        this.orderId = orderId;
        this.productSku = productSku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @NotNull
    @Override
    public String getEventType() {
        return "OrderItemAdded";
    }
}
