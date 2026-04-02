package com.metao.book.order.domain.model.aggregate;

import com.metao.book.order.domain.exception.OrderStateTransitionNotAllowed;
import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.event.DomainInventoryReductionRequestedEvent;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.order.domain.model.event.DomainOrderStatusChangedEvent;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.UserId;
import com.metao.book.shared.domain.base.AggregateRoot;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.ProductTitle;
import com.metao.book.shared.domain.product.Quantity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class OrderAggregate extends AggregateRoot<OrderId> {
    private final OrderId id;
    private final UserId userId;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private Money total;
    private OrderStatus status;
    private Instant updatedAt;

    public OrderAggregate(OrderId id, UserId userId) {
        this(id, userId, new ArrayList<>(), OrderStatus.CREATED, Instant.now(), Instant.now());
    }

    private OrderAggregate(
        OrderId id,
        UserId userId,
        List<OrderItem> items,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        super(Objects.requireNonNull(id, "id can't be null"));
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId can't be null");
        this.items = Objects.requireNonNull(items, "items can't be null");
        this.status = Objects.requireNonNull(status, "status can't be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt can't be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt can't be null");
        this.total = calculateTotal();
    }

    public static OrderAggregate from(com.metao.book.order.domain.event.OrderCreatedEvent event) {
        OrderAggregate order = new OrderAggregate(
            event.orderId(),
            event.userId(),
            new ArrayList<>(),
            event.status(),
            event.createdAt(),
            event.updatedAt()
        );
        event.items().forEach(item ->
            order.items.add(new OrderItem(item.productSku(), item.productTitle(), item.quantity(), item.unitPrice()))
        );
        order.total = order.calculateTotal();
        return order;
    }

    public static OrderAggregate reconstitute(
        OrderId id,
        UserId userId,
        List<OrderItem> items,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new OrderAggregate(id, userId, new ArrayList<>(items), status, createdAt, updatedAt);
    }

    public void addItem(
        ProductSku productSku,
        ProductTitle productTitle,
        Quantity quantity,
        Money unitPrice
    ) {
        Objects.requireNonNull(productSku, "productSku can't be null");
        Objects.requireNonNull(productTitle, "productTitle can't be null");
        Objects.requireNonNull(quantity, "quantity can't be null");
        Objects.requireNonNull(unitPrice, "unitPrice can't be null");
        validateMutableOrder();

        OrderItem existing = items.stream()
            .filter(item -> item.getProductSku().equals(productSku))
            .findFirst()
            .orElse(null);

        if (existing != null) {
            existing.updateQuantity(existing.getQuantity().add(quantity));
        } else {
            items.add(new OrderItem(productSku, productTitle, quantity, unitPrice));
        }

        updatedAt = Instant.now();
        total = calculateTotal();
    }

    public boolean hasItem(ProductSku productSku) {
        return items.stream().anyMatch(item -> item.getProductSku().equals(productSku));
    }

    public void raiseOrderCreatedEvents() {
        addDomainEvent(new DomainOrderCreatedEvent(
            id,
            userId,
            List.copyOf(items),
            total,
            createdAt
        ));
    }

    public synchronized void updateStatus(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus can't be null");
        validateStatusTransition(newStatus);

        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();
        addDomainEvent(new DomainOrderStatusChangedEvent(id, oldStatus, newStatus));
    }

    public synchronized void updateItemQuantity() {
        validateMutableOrder();
        Instant occurredOn = Instant.now();
        items.forEach(item -> {
            addDomainEvent(new DomainInventoryReductionRequestedEvent(occurredOn, item.getProductSku(), item.getQuantity()));
        });
        updatedAt = occurredOn;
        total = calculateTotal();
    }

    public synchronized void removeItem(ProductSku sku) {
        Objects.requireNonNull(sku, "sku can't be null");
        validateMutableOrder();

        boolean removed = items.removeIf(item -> item.getProductSku().equals(sku));
        if (removed) {
            updatedAt = Instant.now();
            total = calculateTotal();
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce((left, right) -> {
                if (!left.currency().equals(right.currency())) {
                    throw new IllegalStateException("Cannot calculate total with different currencies");
                }
                return left.add(right);
            })
            .orElse(null);
    }

    private synchronized void validateStatusTransition(OrderStatus newStatus) {
        if (status == OrderStatus.CREATED && newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
            throw new OrderStateTransitionNotAllowed("Cannot transition from CREATED to " + newStatus);
        }
        if (status == OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
            throw new OrderStateTransitionNotAllowed("Cannot transition from PAID to " + newStatus);
        }
        if (status != OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) {
            throw new OrderStateTransitionNotAllowed("Cannot transition from " + status + " to DELIVERED");
        }
        if (status == OrderStatus.DELIVERED) {
            throw new OrderStateTransitionNotAllowed("Cannot change status of a DELIVERED order");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new OrderStateTransitionNotAllowed("Cannot change status of a CANCELLED order");
        }
    }

    private void validateMutableOrder() {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot modify a " + status + " order");
        }
    }
}
