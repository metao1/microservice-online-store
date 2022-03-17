package com.metao.book.order.domain;

import com.metao.book.shared.domain.base.AbstractAggregateRoot;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import javax.validation.Valid;

@Entity
@Table(name = "order_table")
public class OrderEntity extends AbstractAggregateRoot<OrderId> {

    @Column(name = "product_id", nullable = false)
    private ProductId productId;

    @Column(name = "customer_id", nullable = false)
    private CustomerId customerId;

    @Column(name = "product_count", nullable = false)
    private int productCount;

    @Column(name = "currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public OrderEntity(Status status, ProductId productId, CustomerId customerId, int productCount, Money money) {
        this.status = status;
        this.productId = productId;
        this.customerId = customerId;
        this.productCount = productCount;
        this.currency = money.currency();
        this.amount = money.doubleAmount();
    }

    @SuppressWarnings("unused")
    private OrderEntity() {

    }

    @Transient
    public static OrderEntity of(Status status, @Valid OrderEntity order) {
        return new OrderEntity(order.status, order.productId(), order.customerId, order.productCount,
                new Money(order.currency, order.amount));
    }

    public ProductId productId() {
        return productId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public Status status() {
        return status;
    }

    public int productCount() {
        return productCount;
    }

    public double amount() {
        return amount;
    }

    public Currency currency() {
            return currency;
    }

}
