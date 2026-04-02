package com.metao.book.order.infrastructure.messaging;

import com.metao.book.order.domain.event.OrderCreatedEvent;
import com.metao.book.order.domain.event.OrderCreatedEventItem;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

public record OrderCreatedEventMessage(
    String orderId,
    String userId,
    List<OrderCreatedEventItemMessage> items,
    String status,
    Instant createdAt,
    Instant updatedAt
) {

    public static OrderCreatedEventMessage from(com.metao.book.shared.OrderCreatedEvent event) {
        Instant createdAt = event.hasCreateTime()
            ? Instant.ofEpochSecond(event.getCreateTime().getSeconds(), event.getCreateTime().getNanos())
            : Instant.now();
        Instant updatedAt = event.hasUpdateTime()
            ? Instant.ofEpochSecond(event.getUpdateTime().getSeconds(), event.getUpdateTime().getNanos())
            : createdAt;

        return new OrderCreatedEventMessage(
            event.getId(),
            event.getUserId(),
            mapItems(event),
            event.getStatus().name(),
            createdAt,
            updatedAt
        );
    }

    public OrderCreatedEvent toDomainEvent() {
        return new OrderCreatedEvent(
            OrderId.of(orderId),
            UserId.of(userId),
            items.stream().map(OrderCreatedEventItemMessage::toDomain).toList(),
            OrderStatus.valueOf(status),
            createdAt,
            updatedAt
        );
    }

    private static List<OrderCreatedEventItemMessage> mapItems(com.metao.book.shared.OrderCreatedEvent event) {
        return event.getItemsList().stream()
            .map(item -> new OrderCreatedEventItemMessage(
                item.getSku(),
                item.getProductTitle(),
                BigDecimal.valueOf(item.getQuantity()),
                BigDecimal.valueOf(item.getPrice()),
                item.getCurrency()
            ))
            .toList();
    }

    public record OrderCreatedEventItemMessage(
        String sku,
        String productTitle,
        BigDecimal quantity,
        BigDecimal price,
        String currency
    ) {

        OrderCreatedEventItem toDomain() {
            return new OrderCreatedEventItem(
                ProductSku.of(sku),
                ProductTitle.of(productTitle),
                Quantity.of(quantity),
                Money.of(Currency.getInstance(currency), price)
            );
        }
    }
}
