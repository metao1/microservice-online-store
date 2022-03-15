package com.metao.book.order.domain;

import com.metao.book.shared.domain.base.AbstractAggregateRoot;
import com.metao.book.shared.domain.financial.Currency;
import com.metao.book.shared.domain.financial.Money;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;
import javax.validation.Valid;

@Entity
@Table(name="order")
public class Order extends AbstractAggregateRoot<OrderId> {

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

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

    Order(Status status, ProductId productId, CustomerId customerId, int productCount, Money money) {
        this.status = status;
        this.productId = productId;
        this.customerId = customerId;
        this.productCount = productCount;
        this.currency = money.currency();
        this.amount = money.doubleAmount();
    }

    @SuppressWarnings("unused")
    private Order() {

    }

    @Transient
    public static Order of(Status status, @Valid Order order) {
        return new Order(order.status, order.productId(), order.customerId, order.productCount,
                new Money(order.currency, order.amount)
        );
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
}
