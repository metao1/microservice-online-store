package com.metao.book.order.domain.model.aggregate;

import com.metao.book.order.domain.exception.OrderStateTransitionNotAllowed;
import com.metao.book.order.domain.model.entity.OrderItem;
import com.metao.book.order.domain.model.event.DomainInventoryReductionRequestedEvent;
import com.metao.book.order.domain.model.event.DomainOrderCreatedEvent;
import com.metao.book.order.domain.model.event.DomainOrderItemAddedEvent;
import com.metao.book.order.domain.model.event.DomainOrderStatusChangedEvent;
import com.metao.book.order.domain.model.valueobject.CustomerId;
import com.metao.book.order.domain.model.valueobject.OrderId;
import com.metao.book.order.domain.model.valueobject.OrderStatus;
import com.metao.book.shared.domain.base.AggregateRoot;
import com.metao.book.shared.domain.financial.Money;
import com.metao.book.shared.domain.product.ProductSku;
import com.metao.book.shared.domain.product.Quantity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class OrderAggregate extends AggregateRoot<OrderId> {
    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private Money total;
    private OrderStatus status;
    private Instant updatedAt;

    public OrderAggregate(@NotNull OrderId id, @NotNull CustomerId customerId) {
        super(id);
        this.id = java.util.Objects.requireNonNull(id, "Order ID cannot be null");
        this.customerId = java.util.Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.items = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.total = calculateTotal();
        this.status = OrderStatus.CREATED;

        // Raise OrderCreatedEvent
        addDomainEvent(new DomainOrderCreatedEvent(id, customerId));
    }

    public void addItem(@NotNull ProductSku productSku, @NotNull Quantity quantity, @NotNull Money unitPrice) {
        java.util.Objects.requireNonNull(productSku, "Product ID cannot be null");
        // Validate order state
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot add items to a " + status + " order");
        }

        // Merge quantities when product already exists instead of failing
        var existing = items.stream()
            .filter(item -> item.getProductSku().equals(productSku))
            .findFirst()
            .orElse(null);
        if (existing != null) {
            existing.updateQuantity(existing.getQuantity().add(quantity));
        } else {
            OrderItem item = new OrderItem(productSku, quantity, unitPrice);
            this.items.add(item);
        }
        this.updatedAt = Instant.now();
        this.total = calculateTotal();

        // Raise OrderItemAddedEvent
        addDomainEvent(new DomainOrderItemAddedEvent(id, productSku, quantity, unitPrice));
    }

    public synchronized void updateStatus(@NotNull OrderStatus newStatus) {
        java.util.Objects.requireNonNull(newStatus, "New status cannot be null");
        // Validate status transition
        validateStatusTransition(newStatus);

        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();

        // Raise OrderStatusChangedEvent
        addDomainEvent(new DomainOrderStatusChangedEvent(id, oldStatus, newStatus));
    }

    public synchronized void removeItem(@NotNull ProductSku productId) {
        // Validate order state
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot remove items from a " + status + " order");
        }

        boolean removed = items.removeIf(item -> item.getProductSku().equals(productId));
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

    public synchronized void updateItemQuantity() {
        // Validate order state
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot update items in a " + status + " order");
        }
        items.forEach(item -> {
            item.updateQuantity(item.getQuantity().add(Quantity.of(java.math.BigDecimal.ONE)));
            this.updatedAt = Instant.now();
            addDomainEvent(
                new DomainInventoryReductionRequestedEvent(updatedAt, item.getProductSku(), item.getQuantity())
            );
        });
        this.total = calculateTotal();
    }

    private synchronized void validateStatusTransition(OrderStatus newStatus) {
        if (status == OrderStatus.CREATED && newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
            throw new OrderStateTransitionNotAllowed("Cannot transition from CREATED to " + newStatus);
        } else if (status == OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
            throw new OrderStateTransitionNotAllowed("Cannot transition from PAID to " + newStatus);
        } else if (status != OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) {
            throw new OrderStateTransitionNotAllowed("Cannot transition from " + status + " to DELIVERED");
        } else if (status == OrderStatus.DELIVERED) {
            throw new OrderStateTransitionNotAllowed("Cannot change status of a DELIVERED order");
        } else if (status == OrderStatus.CANCELLED) {
            throw new OrderStateTransitionNotAllowed("Cannot change status of a CANCELLED order");
        }
    }

    public synchronized Money getTotal() {
        return total;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
