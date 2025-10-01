package com.metao.book.order.domain.model.aggregate;

import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.order.domain.model.event.OrderItemAddedEvent;
import com.metao.book.order.domain.model.event.OrderStatusChangedEvent;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.order.domain.model.valueobject.ProductId;
import com.metao.book.order.domain.model.valueobject.Quantity;
import com.metao.book.shared.domain.base.DomainEvent;
import com.metao.book.shared.domain.financial.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class OrderAggregate {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private Money total;
    private OrderStatus status;
    private Instant updatedAt;

    public OrderAggregate(OrderId id, CustomerId customerId) {
        Objects.requireNonNull(id, "Order ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");

        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.total = calculateTotal();

        // Raise OrderCreatedEvent
        domainEvents.add(new DomainOrderCreatedEvent(id, customerId));
    }

    public void addItem(ProductId productId, String productName, Quantity quantity, Money unitPrice) {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        // Validate order state
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot add items to a " + status + " order");
        }

        // Check for duplicate product
        if (items.stream().anyMatch(item -> item.getProductId().equals(productId))) {
            throw new IllegalStateException("Product already exists in order");
        }

        // Create and add order item
        OrderItem item = new OrderItem(productId, productName, quantity, unitPrice);
        this.items.add(item);
        this.updatedAt = Instant.now();
        this.total = calculateTotal();

        // Raise OrderItemAddedEvent
        domainEvents.add(new OrderItemAddedEvent(
            id,
            productId,
            productName,
            quantity,
            unitPrice));
    }

    public synchronized void updateStatus(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "New status cannot be null");

        // Validate status transition
        validateStatusTransition(newStatus);

        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();

        // Raise OrderStatusChangedEvent
        domainEvents.add(new OrderStatusChangedEvent(id, oldStatus, newStatus));
    }

    public synchronized void removeItem(ProductId productId) {
        Objects.requireNonNull(productId, "Product ID cannot be null");

        // Validate order state
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot remove items from a " + status + " order");
        }

        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (removed) {
            this.updatedAt = Instant.now();
            this.total = calculateTotal();
        }
    }

    private Money calculateTotal() {
        return items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce((m1, m2) -> {
                if (!m1.currency().equals(m2.currency())) {
                    throw new IllegalStateException("Cannot calculate total with different currencies");
                } else {
                    return m1.add(m2);
                }
            }).orElse(null);
    }

    public synchronized void updateItemQuantity(ProductId productId, Quantity newQuantity) {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(newQuantity, "New quantity cannot be null");

        // Validate order state
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot update items in a " + status + " order");
        }

        items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                item.updateQuantity(newQuantity);
                this.updatedAt = Instant.now();
                this.total = calculateTotal();
            });
    }

    public synchronized Money getTotal() {
        return total;
    }

    private synchronized void validateStatusTransition(OrderStatus newStatus) {
        if (status == OrderStatus.CREATED && newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition from CREATED to " + newStatus);
        } else if (status == OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition from PAID to " + newStatus);
        } else if (status != OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot transition from " + status + " to DELIVERED");
        } else if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot change status of a DELIVERED order");
        } else if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a CANCELLED order");
        }
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}